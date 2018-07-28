package com.axel_stein.domain.interactor.note;

import android.support.annotation.NonNull;

import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.model.NoteOrder;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.domain.repository.NoteRepository;
import com.axel_stein.domain.repository.SettingsRepository;
import com.axel_stein.domain.utils.validators.LabelValidator;
import com.axel_stein.domain.utils.validators.NotebookValidator;

import org.joda.time.DateTime;

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
import static com.axel_stein.domain.utils.validators.NoteValidator.isValid;

public class QueryNoteInteractor {

    @NonNull
    private NoteRepository mNoteRepository;

    @NonNull
    private SettingsRepository mSettingsRepository;

    public QueryNoteInteractor(@NonNull NoteRepository n, @NonNull SettingsRepository s) {
        mNoteRepository = requireNonNull(n);
        mSettingsRepository = requireNonNull(s);
    }

    @NonNull
    public Single<List<Note>> queryAll() {
        return single(new Callable<List<Note>>() {
            @Override
            public List<Note> call() {
                return orderImpl(mNoteRepository.queryAll());
            }
        });
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
                return trashOrderImpl(mNoteRepository.queryTrashed());
            }
        });
    }

    /**
     * @throws NullPointerException     if notebook is null
     * @throws IllegalArgumentException if notebook`s id is 0
     */
    @NonNull
    public Single<List<Note>> query(@NonNull final Notebook notebook) {
        String id = notebook.getId();
        if (id != null) {
            switch (id) {
                case Notebook.ID_ALL:
                    return queryAll();

                case Notebook.ID_STARRED: // todo cache
                    return queryStarred();
            }
        }

        final String key = "notebook_" + notebook.getId();
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
                if (!NotebookValidator.isValid(notebook)) {
                    throw new IllegalArgumentException("notebook is not valid");
                }
                List<Note> notes = orderImpl(mNoteRepository.queryNotebook(notebook));
                put(key, notes);
                return notes;
            }
        });
    }

    /**
     * @throws NullPointerException     if label is null
     * @throws IllegalArgumentException if label`s id is 0
     */
    @NonNull
    public Single<List<Note>> query(@NonNull final Label label) {
        final String key = "label_" + label.getId();
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
                if (!LabelValidator.isValid(label)) {
                    throw new IllegalArgumentException("label is not valid");
                }
                List<Note> notes = orderImpl(mNoteRepository.queryLabel(label));
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
                q = requireNonNull(q, "query is null");
                if (isEmpty(query)) {
                    throw new IllegalArgumentException("query is empty");
                }
                q = query.toLowerCase();

                StringBuilder builder = new StringBuilder();

                List<Note> notes = orderImpl(mNoteRepository.search(query), NoteOrder.VIEWS, true);
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
                        builder.append(content.substring(start, end));

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
                        n1 = requireNonNull(n1, "note1 is null");
                        n2 = requireNonNull(n2, "note2 is null");

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
        order = requireNonNull(order, "order is null");
        return orderImpl(list, order, false);
    }

    @NonNull
    private List<Note> trashOrderImpl(@NonNull List<Note> list) {
        return orderImpl(list, NoteOrder.TRASHED, false);
    }

    @NonNull
    private List<Note> orderImpl(@NonNull List<Note> list, @NonNull final NoteOrder order, boolean searchFlag) {
        if (!isValid(list)) {
            throw new IllegalStateException("list is not valid");
        }

        // set labels
        for (Note note : list) {
            // todo note.setLabels(mNoteLabelPairRepository.queryLabels(note));
            String content = note.getContent();
            if (!isEmpty(content)) {
                if (mSettingsRepository.showNotesContent() || searchFlag) {
                    content = content.replace('\n', ' ');
                    content = content.replaceAll(" [ ]+", " ");
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
                note.setTitle(title);
            }
        }

        Collections.sort(list, new Comparator<Note>() {
            @Override
            public int compare(Note n1, Note n2) {
                n1 = requireNonNull(n1, "note1 is null");
                n2 = requireNonNull(n2, "note2 is null");

                boolean desc = order.isDesc();

                switch (order) {
                    case TITLE:
                        if (desc) {
                            return n2.getTitle().compareTo(n1.getTitle());
                        }
                        return n1.getTitle().compareTo(n2.getTitle());

                    case VIEWS:
                        return compareViews(n1.getViews(), n2.getViews(), desc);

                    case CREATED:
                        return compareDates(n1.getCreatedDate(), n2.getCreatedDate(), desc);

                    case MODIFIED:
                        return compareDates(n1.getCreatedDate(), n2.getCreatedDate(), desc);

                    case TRASHED:
                        return compareDates(n1.getTrashedDate(), n2.getTrashedDate(), false);
                }

                return 0;
            }
        });

        if (!searchFlag) {
            Collections.sort(list, new Comparator<Note>() {
                @Override
                public int compare(Note n1, Note n2) {
                    n1 = requireNonNull(n1, "note1 is null");
                    n2 = requireNonNull(n2, "note2 is null");

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

    private int compareDates(DateTime d1, DateTime d2, boolean desc) {
        if (d1 == null || d2 == null) {
            return 0;
        }
        /*
        else if (d2 == null) {
            return desc ? 1 : -1;
        } else if (d1 == null) {
            return desc ? -1 : 1;
        }
        */

        if (d1.isEqual(d2)) {
            return 0;
        } else if (d1.isAfter(d2)) {
            return desc ? 1 : -1;
        } else {
            return desc ? -1 : 1;
        }
    }

    private int compareViews(long r1, long r2, boolean desc) {
        long d = r1 - r2;
        if (d == 0) {
            return 0;
        } else if (d > 0) {
            return desc ? 1 : -1;
        } else {
            return desc ? -1 : 1;
        }
    }

}
