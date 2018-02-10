package com.axel_stein.domain.interactor.note;

import android.support.annotation.NonNull;

import com.axel_stein.domain.model.Label;
import com.axel_stein.domain.model.Note;
import com.axel_stein.domain.model.NoteOrder;
import com.axel_stein.domain.model.Notebook;
import com.axel_stein.domain.repository.NoteLabelPairRepository;
import com.axel_stein.domain.repository.NoteRepository;
import com.axel_stein.domain.repository.SettingsRepository;
import com.axel_stein.domain.utils.validators.LabelValidator;
import com.axel_stein.domain.utils.validators.NotebookValidator;

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

    @NonNull
    private NoteLabelPairRepository mNoteLabelPairRepository;

    public QueryNoteInteractor(@NonNull NoteRepository noteRepository,
                               @NonNull SettingsRepository settingsRepository,
                               @NonNull NoteLabelPairRepository helperRepository) {
        mNoteRepository = requireNonNull(noteRepository, "noteStorage is null");
        mSettingsRepository = requireNonNull(settingsRepository, "settingsStorage is null");
        mNoteLabelPairRepository = requireNonNull(helperRepository, "helperRepository is null");
    }

    /**
     * @return all notes, including trash
     */
    @NonNull
    public Single<List<Note>> executeHome() {
        final String key = "home";
        if (hasKey(key)) {
            return single(new Callable<List<Note>>() {
                @Override
                public List<Note> call() throws Exception {
                    return get(key);
                }
            });
        }
        return single(new Callable<List<Note>>() {
            @Override
            public List<Note> call() throws Exception {
                List<Note> notes = orderImpl(mNoteRepository.queryHome());
                put(key, notes);
                return notes;
            }
        });
    }

    /**
     * @return all notes, including trash
     */
    @NonNull
    public Single<List<Note>> execute() {
        return single(new Callable<List<Note>>() {
            @Override
            public List<Note> call() throws Exception {
                return orderImpl(mNoteRepository.query());
            }
        });
    }

    /**
     * @return notes in notebook
     * @throws NullPointerException     if notebook is null
     * @throws IllegalArgumentException if notebook`s id is 0
     */
    @NonNull
    public Single<List<Note>> execute(@NonNull final Notebook notebook) {
        final String key = "notebook_" + notebook.getId();
        if (hasKey(key)) {
            return single(new Callable<List<Note>>() {
                @Override
                public List<Note> call() throws Exception {
                    return get(key);
                }
            });
        }
        return single(new Callable<List<Note>>() {
            @Override
            public List<Note> call() throws Exception {
                if (!NotebookValidator.isValid(notebook)) {
                    throw new IllegalArgumentException("notebook is not valid");
                }
                List<Note> notes = orderImpl(mNoteRepository.query(notebook));
                put(key, notes);
                return notes;
            }
        });
    }

    /**
     * @return notes that contain label
     * @throws NullPointerException     if label is null
     * @throws IllegalArgumentException if label`s id is 0
     */
    @NonNull
    public Single<List<Note>> execute(@NonNull final Label label) {
        final String key = "label_" + label.getId();
        if (hasKey(key)) {
            return single(new Callable<List<Note>>() {
                @Override
                public List<Note> call() throws Exception {
                    return get(key);
                }
            });
        }
        return single(new Callable<List<Note>>() {
            @Override
            public List<Note> call() throws Exception {
                if (!LabelValidator.isValid(label)) {
                    throw new IllegalArgumentException("label is not valid");
                }
                List<Note> notes = orderImpl(mNoteRepository.query(label));
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
            public List<Note> call() throws Exception {
                q = requireNonNull(q, "query is null");
                if (isEmpty(query)) {
                    throw new IllegalArgumentException("query is empty");
                }
                q = query.toLowerCase();

                StringBuilder builder = new StringBuilder();

                List<Note> notes = orderImpl(mNoteRepository.search(query), NoteOrder.RELEVANCE, true);
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

    /**
     * @return notes in trash
     */
    @NonNull
    public Single<List<Note>> queryTrash() {
        return single(new Callable<List<Note>>() {
            @Override
            public List<Note> call() throws Exception {
                return orderImpl(mNoteRepository.queryTrash());
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
    private List<Note> orderImpl(@NonNull List<Note> list, @NonNull final NoteOrder order, boolean searchFlag) {
        if (!isValid(list)) {
            throw new IllegalStateException("list is not valid");
        }

        // set labels
        for (Note note : list) {
            note.setLabels(mNoteLabelPairRepository.queryLabelsOfNote(note));

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

                switch (order) {
                    case TITLE:
                        return n1.getTitle().compareTo(n2.getTitle());

                    case RELEVANCE:
                        return compareDates(n1.getRelevance(), n2.getRelevance(), false);

                    case DATE_NEWEST:
                        return compareDates(n1.getDate(), n2.getDate(), false);

                    case DATE_OLDEST:
                        return compareDates(n1.getDate(), n2.getDate(), true);

                    case UPDATE_NEWEST:
                        return compareDates(n1.getUpdate(), n2.getUpdate(), false);

                    case UPDATE_OLDEST:
                        return compareDates(n1.getUpdate(), n2.getUpdate(), true);
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

    private int compareDates(long date1, long date2, boolean reverse) {
        long d = date1 - date2;
        if (d == 0) {
            return 0;
        } else if (d > 0) {
            return reverse ? 1 : -1;
        } else {
            return reverse ? -1 : 1;
        }
    }

}
