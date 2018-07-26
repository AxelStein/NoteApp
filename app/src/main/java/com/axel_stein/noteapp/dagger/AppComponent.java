package com.axel_stein.noteapp.dagger;

import com.axel_stein.noteapp.backup.BackupPresenter;
import com.axel_stein.noteapp.base.BaseActivity;
import com.axel_stein.noteapp.dialogs.label.AddLabelDialog;
import com.axel_stein.noteapp.dialogs.label.DeleteLabelDialog;
import com.axel_stein.noteapp.dialogs.label.RenameLabelDialog;
import com.axel_stein.noteapp.dialogs.note.DeleteNoteDialog;
import com.axel_stein.noteapp.dialogs.notebook.AddNotebookDialog;
import com.axel_stein.noteapp.dialogs.notebook.DeleteNotebookDialog;
import com.axel_stein.noteapp.dialogs.notebook.RenameNotebookDialog;
import com.axel_stein.noteapp.label_manager.LabelManagerFragment;
import com.axel_stein.noteapp.label_manager.LabelManagerPresenter;
import com.axel_stein.noteapp.main.HomeFragment;
import com.axel_stein.noteapp.main.LabelsFragment;
import com.axel_stein.noteapp.main.MainActivity;
import com.axel_stein.noteapp.main.NoteListActivity;
import com.axel_stein.noteapp.main.NotebooksFragment;
import com.axel_stein.noteapp.main.TrashFragment;
import com.axel_stein.noteapp.main.UserActivity;
import com.axel_stein.noteapp.notebook_manager.NotebookManagerFragment;
import com.axel_stein.noteapp.notebook_manager.NotebookManagerPresenter;
import com.axel_stein.noteapp.notes.edit.EditNoteActivity;
import com.axel_stein.noteapp.notes.edit.EditNoteFragment;
import com.axel_stein.noteapp.notes.edit.EditNotePresenter;
import com.axel_stein.noteapp.notes.list.DrawerHelper;
import com.axel_stein.noteapp.notes.list.NotesActivity;
import com.axel_stein.noteapp.notes.list.TrashActivity;
import com.axel_stein.noteapp.notes.list.presenters.HomeNotesPresenter;
import com.axel_stein.noteapp.notes.list.presenters.LabelNotesPresenter;
import com.axel_stein.noteapp.notes.list.presenters.NotebookNotesPresenter;
import com.axel_stein.noteapp.notes.list.presenters.NotesPresenter;
import com.axel_stein.noteapp.notes.list.presenters.SearchNotesPresenter;
import com.axel_stein.noteapp.notes.list.presenters.TrashNotesPresenter;
import com.axel_stein.noteapp.settings.SettingsPresenter;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
        AppModule.class, LabelInteractorModule.class,
        NotebookInteractorModule.class, NoteInteractorModule.class,
        BackupInteractorModule.class
})

public interface AppComponent {

    void inject(NotebookNotesPresenter presenter);

    void inject(AddNotebookDialog dialog);

    void inject(RenameNotebookDialog dialog);

    void inject(DeleteNotebookDialog dialog);

    void inject(AddLabelDialog dialog);

    void inject(RenameLabelDialog dialog);

    void inject(DeleteLabelDialog dialog);

    void inject(DrawerHelper helper);

    void inject(NotesActivity activity);

    void inject(TrashActivity activity);

    void inject(MainActivity activity);

    void inject(BaseActivity activity);

    void inject(NoteListActivity activity);

    void inject(EditNoteActivity activity);

    void inject(TrashNotesPresenter presenter);

    void inject(SearchNotesPresenter presenter);

    void inject(LabelNotesPresenter presenter);

    void inject(EditNotePresenter presenter);

    void inject(NotesPresenter presenter);

    void inject(HomeNotesPresenter presenter);

    void inject(EditNoteFragment fragment);

    void inject(BackupPresenter presenter);

    void inject(NotebookManagerPresenter presenter);

    void inject(NotebookManagerFragment fragment);

    void inject(LabelManagerFragment fragment);

    void inject(LabelManagerPresenter presenter);

    void inject(DeleteNoteDialog dialog);

    void inject(NotebooksFragment fragment);

    void inject(LabelsFragment fragment);

    void inject(TrashFragment fragment);

    void inject(HomeFragment fragment);

    void inject(SettingsPresenter presenter);

    void inject(UserActivity activity);

}
