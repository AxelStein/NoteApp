package com.axel_stein.noteapp.dagger;

import com.axel_stein.noteapp.App;
import com.axel_stein.noteapp.base.BaseActivity;
import com.axel_stein.noteapp.dialogs.note.DeleteNoteDialog;
import com.axel_stein.noteapp.dialogs.notebook.AddNotebookDialog;
import com.axel_stein.noteapp.dialogs.notebook.DeleteNotebookDialog;
import com.axel_stein.noteapp.dialogs.notebook.RenameNotebookDialog;
import com.axel_stein.noteapp.google_drive.DriveWorker;
import com.axel_stein.noteapp.main.InboxFragment;
import com.axel_stein.noteapp.main.MainActivity;
import com.axel_stein.noteapp.main.NotebookNotesFragment;
import com.axel_stein.noteapp.main.StarredFragment;
import com.axel_stein.noteapp.main.TrashFragment;
import com.axel_stein.noteapp.main.UserActivity;
import com.axel_stein.noteapp.main.edit.EditNoteActivity;
import com.axel_stein.noteapp.main.edit.EditNoteFragment;
import com.axel_stein.noteapp.main.edit.EditNotePresenter;
import com.axel_stein.noteapp.main.list.presenters.InboxNotesPresenter;
import com.axel_stein.noteapp.main.list.presenters.NotebookNotesPresenter;
import com.axel_stein.noteapp.main.list.presenters.NotesPresenter;
import com.axel_stein.noteapp.main.list.presenters.SearchNotesPresenter;
import com.axel_stein.noteapp.main.list.presenters.StarredNotesPresenter;
import com.axel_stein.noteapp.main.list.presenters.TrashNotesPresenter;
import com.axel_stein.noteapp.settings.SettingsPresenter;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
        AppModule.class, NotebookInteractorModule.class,
        NoteInteractorModule.class, BackupInteractorModule.class
})

public interface AppComponent {

    void inject(StarredFragment fragment);

    void inject(NotebookNotesPresenter presenter);

    void inject(AddNotebookDialog dialog);

    void inject(RenameNotebookDialog dialog);

    void inject(DeleteNotebookDialog dialog);

    void inject(MainActivity activity);

    void inject(BaseActivity activity);

    void inject(UserActivity activity);

    void inject(EditNoteActivity activity);

    void inject(TrashNotesPresenter presenter);

    void inject(SearchNotesPresenter presenter);

    void inject(EditNotePresenter presenter);

    void inject(NotesPresenter presenter);

    void inject(StarredNotesPresenter presenter);

    void inject(InboxNotesPresenter presenter);

    void inject(EditNoteFragment fragment);

    void inject(DeleteNoteDialog dialog);

    void inject(TrashFragment fragment);

    void inject(InboxFragment fragment);

    void inject(SettingsPresenter presenter);

    void inject(NotebookNotesFragment fragment);

    void inject(DriveWorker worker);

    void inject(App app);

}
