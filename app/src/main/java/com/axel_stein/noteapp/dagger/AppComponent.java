package com.axel_stein.noteapp.dagger;

import com.axel_stein.noteapp.backup.BackupPresenter;
import com.axel_stein.noteapp.base.BaseActivity;
import com.axel_stein.noteapp.dialogs.label.AddLabelDialog;
import com.axel_stein.noteapp.dialogs.label.DeleteLabelDialog;
import com.axel_stein.noteapp.dialogs.label.RenameLabelDialog;
import com.axel_stein.noteapp.dialogs.notebook.AddNotebookDialog;
import com.axel_stein.noteapp.dialogs.notebook.DeleteNotebookDialog;
import com.axel_stein.noteapp.dialogs.notebook.RenameNotebookDialog;
import com.axel_stein.noteapp.dialogs.notebook.SelectNotebookDialog;
import com.axel_stein.noteapp.notes.edit.EditNoteActivity;
import com.axel_stein.noteapp.notes.edit.EditNoteFragment;
import com.axel_stein.noteapp.notes.edit.EditNotePresenter;
import com.axel_stein.noteapp.notes.list.DrawerHelper;
import com.axel_stein.noteapp.notes.list.NotesActivity;
import com.axel_stein.noteapp.notes.list.TrashActivity;
import com.axel_stein.noteapp.notes.list.presenters.LabelNotesPresenter;
import com.axel_stein.noteapp.notes.list.presenters.NotebookNotesPresenter;
import com.axel_stein.noteapp.notes.list.presenters.NotesPresenter;
import com.axel_stein.noteapp.notes.list.presenters.SearchNotesPresenter;
import com.axel_stein.noteapp.notes.list.presenters.TrashNotesPresenter;
import com.axel_stein.noteapp.settings.SettingsFragment;
import com.axel_stein.noteapp.views.LinedEditText;

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

    void inject(DrawerHelper drawerHelper);

    void inject(NotesActivity activity);

    void inject(TrashActivity trashActivity);

    void inject(BaseActivity baseActivity);

    void inject(EditNoteActivity activity);

    void inject(TrashNotesPresenter presenter);

    void inject(SearchNotesPresenter searchListNotesPresenter);

    void inject(LabelNotesPresenter labelNotesPresenter);

    void inject(SettingsFragment settingsFragment);

    void inject(EditNotePresenter editNotePresenter);

    void inject(NotesPresenter notesPresenter);

    void inject(SelectNotebookDialog selectNotebookDialog);

    void inject(EditNoteFragment editNoteFragment);

    void inject(BackupPresenter backupPresenter);

    void inject(LinedEditText linedEditText);

}
