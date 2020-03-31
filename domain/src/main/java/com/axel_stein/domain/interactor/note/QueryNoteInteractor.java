package com.axel_stein.domain.interactor.note;

import androidx.annotation.NonNull;

import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.model.NoteOrder;
import com.axel_stein.domain.repository.NoteRepository;
import com.axel_stein.domain.repository.SettingsRepository;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import static com.axel_stein.domain.model.NoteCache.get;
import static com.axel_stein.domain.model.NoteCache.hasKey;
import static com.axel_stein.domain.model.NoteCache.put;
import static com.axel_stein.domain.utils.ObjectUtil.requireNonNull;
import static com.axel_stein.domain.utils.TextUtil.isEmpty;

public class QueryNoteInteractor {

    @NonNull
    private final NoteRepository mNoteRepository;

    @NonNull
    private final SettingsRepository mSettingsRepository;

    public QueryNoteInteractor(@NonNull NoteRepository n, @NonNull SettingsRepository s) {
        mNoteRepository = requireNonNull(n);
        mSettingsRepository = requireNonNull(s);
    }

    @NonNull
    public Single<List<Note>> queryInbox() {
        final String key = "inbox";
        if (hasKey(key)) {
            return single(new Callable<List<Note>>() {
                @Override
                public List<Note> call() {
                    return get(key);
                }
            });
        }
        return single(new Callable<List<Note>>() {
            @Override
            public List<Note> call() {
                List<Note> notes = orderImpl(mNoteRepository.queryInbox());
                put(key, notes);
                return notes;
            }
        });
    }

    @NonNull
    public Single<List<Note>> queryStarred() {
        return single(new Callable<List<Note>>() {
            @Override
            public List<Note> call() {
                return orderImpl(mNoteRepository.queryStarred());
            }
        });
    }

    @NonNull
    public Single<List<Note>> queryTrashed() {
        return single(new Callable<List<Note>>() {
            @Override
            public List<Note> call() {
                List<Note> result = mNoteRepository.queryTrashed();
                LocalDate today = new LocalDate();
                for (Note note : result) {
                    LocalDate date = new LocalDate(note.getTrashedDate());
                    date = date.plusDays(7);
                    if (date.equals(today) || date.isBefore(today)) {
                        mNoteRepository.delete(note);
                    }
                }
                return trashOrderImpl(mNoteRepository.queryTrashed());
            }
        });
    }

    /**
     * @throws NullPointerException     if notebook is null
     * @throws IllegalArgumentException if notebook`s id is 0
     */
    @NonNull
    public Single<List<Note>> query(@NonNull final String notebookId) {
        final String key = "notebook_" + notebookId;
        if (hasKey(key)) {
            return single(new Callable<List<Note>>() {
                @Override
                public List<Note> call() {
                    return get(key);
                }
            });
        }
        return single(new Callable<List<Note>>() {
            @Override
            public List<Note> call() {
                if (isEmpty(notebookId)) {
                    throw new IllegalArgumentException("notebook is not valid");
                }
                List<Note> notes = orderImpl(mNoteRepository.queryNotebook(notebookId));
                put(key, notes);
                return notes;
            }
        });
    }

    /**
     * @param query search query
     * @return notes that match query
     * @throws NullPointerException     if query is null
     * @throws IllegalArgumentException if query is empty
     */
    @NonNull
    public Single<List<Note>> search(@NonNull final String query) {
        return single(new Callable<List<Note>>() {
            private String q = query;

            @Override
            public List<Note> call() {
                if (isEmpty(q)) {
                    return new ArrayList<>();
                }
                q = query.toLowerCase();

                StringBuilder builder = new StringBuilder();

                List<Note> notes = orderImpl(mNoteRepository.search(q), NoteOrder.VIEWS, true);
                for (Note note : notes) {
                    String content = note.getContent();
                    if (content != null) {
                        content = content.toLowerCase();
                    }
                    if (!isEmpty(content) && content.contains(q)) {
                        int start = content.indexOf(q);

                        for (int i = start-1; i >= 0; i--) {
                            char c = content.charAt(i);
                            if (c == ' ' || i == 0) {
                                start = i + (i == 0 ? 0 : 1);
                                break;
                            }
                        }

                        if (start > 0) {
                            builder.append("...");
                        }

                        int end = start + 128;
                        if (end > content.length()) {
                            end = content.length();
                        }
                        builder.append(content, start, end);

                        content = builder.toString();
                        note.setContent(content);
                        builder.delete(0, builder.length());
                    } else {
                        note.setContent(null);
                    }
                }

                Collections.sort(notes, new Comparator<Note>() {
                    @Override
                    public int compare(Note n1, Note n2) {
                        requireNonNull(n1, "note1 is null");
                        requireNonNull(n2, "note2 is null");

                        String title1 = n1.getTitle().toLowerCase();
                        String title2 = n2.getTitle().toLowerCase();

                        boolean contains1 = title1.contains(q);
                        boolean starts1 = title1.startsWith(q);

                        boolean contains2 = title2.contains(q);
                        boolean starts2 = title2.startsWith(q);

                        if (contains1 && contains2) {
                            if (starts1 && starts2) {
                                return 0;
                            } else if (starts1) {
                                return -1;
                            } else {
                                return 1;
                            }
                        } else if (!contains1 && !contains2) {
                            return 0;
                        } else if (contains1) {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                });

                return notes;
            }
        });
    }

    private Single<List<Note>> single(Callable<List<Note>> callable) {
        return Single.fromCallable(callable)
                .subscribeOn(Schedulers.io());
    }

    @NonNull
    private List<Note> orderImpl(@NonNull List<Note> list) {
        NoteOrder order = mSettingsRepository.getNotesOrder();
        requireNonNull(order, "order is null");
        return orderImpl(list, order, false);
    }

    @NonNull
    private List<Note> trashOrderImpl(@NonNull List<Note> list) {
        return orderImpl(list, NoteOrder.TRASHED, false);
    }

    @NonNull
    private List<Note> orderImpl(@NonNull List<Note> list, @NonNull final NoteOrder order, boolean searchFlag) {
        /*
        todo
        if (!isValid(list)) {
            throw new IllegalStateException("list is not valid");
        }
        */

        for (Note note : list) {
            String content = note.getContent();
            if (!isEmpty(content)) {
                if (mSettingsRepository.showNotesContent() || searchFlag) {
                    content = content.replace('\n', ' ');
                    content = content.replaceAll(" [ ]+", " ");
                    content = content.trim();
                    if (!searchFlag && content.length() > 128) {
                        content = content.substring(0, 128);
                    }
                    note.setContent(content);
                } else {
                    note.setContent(null);
                }
            }

            String title = note.getTitle();
            if (isEmpty(title)) {
                note.setTitle(content);
                note.setContent(null);
            } else {
                title = title.replace('\n', ' ');
                title = title.replaceAll(" [ ]+", " ");
                title = title.trim();
                note.setTitle(title);
            }
        }

        Collections.sort(list, new Comparator<Note>() {
            @Override
            public int compare(Note n1, Note n2) {
                requireNonNull(n1, "note1 is null");
                requireNonNull(n2, "note2 is null");

                switch (order) {
                    case TITLE:
                        return n1.getTitle().compareTo(n2.getTitle());

                    case VIEWS:
                        return compareViews(n1.getViews(), n2.getViews());

                    case MODIFIED:
                        return compareDates(n1.getModifiedDate(), n2.getModifiedDate());

                    case TRASHED:
                        return compareDates(n1.getTrashedDate(), n2.getTrashedDate());
                }

                return 0;
            }
        });

        if (!searchFlag && order != NoteOrder.TRASHED) {
            Collections.sort(list, new Comparator<Note>() {
                @Override
                public int compare(Note n1, Note n2) {
                    requireNonNull(n1, "note1 is null");
                    requireNonNull(n2, "note2 is null");

                    boolean p1 = n1.isPinned();
                    boolean p2 = n2.isPinned();

                    if (p1 && !p2) {
                        return -1;
                    } else if (!p1 && p2) {
                        return 1;
                    }
                    return 0;
                }
            });
        }

        return list;
    }

    private int compareDates(DateTime d1, DateTime d2) {
        if (d1 == null || d2 == null) {
            return 0;
        }

        if (d1.isEqual(d2)) {
            return 0;
        } else if (d1.isAfter(d2)) {
            return -1;
        } else {
            return 1;
        }
    }

    private int compareViews(long r1, long r2) {
        long d = r1 - r2;
        if (d == 0) {
            return 0;
        } else if (d > 0) {
            return -1;
        } else {
            return 1;
        }
    }

}
