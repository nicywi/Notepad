package com.nkcdev.notepad;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private NoteViewModel noteViewModel;

    ActivityResultLauncher<Intent> activityResultLauncherForAddNote;
    ActivityResultLauncher<Intent> activityResultLauncherForUpdateNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerActivityForAddNote();
        registerActivityForUpdateNote();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        NoteAdapter adapter = new NoteAdapter();
        recyclerView.setAdapter(adapter);

        noteViewModel = new ViewModelProvider.AndroidViewModelFactory(getApplication())
                .create(NoteViewModel.class);

        noteViewModel.getAllNotes().observe(this, new Observer<List<Note>>() {
            @Override
            public void onChanged(List<Note> notes) {
                //update Recycler View
                adapter.setNotes(notes);
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                noteViewModel.delete(adapter.getNotes(viewHolder.getAdapterPosition()));
                Toast.makeText(getApplicationContext(), "NOte DEleted", Toast.LENGTH_LONG).show();
            }
        }).attachToRecyclerView(recyclerView);

        adapter.setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Note note) {

                Intent intent = new Intent(MainActivity.this, UpdateActivity.class);
                intent.putExtra("id", note.getId());
                intent.putExtra("title", note.getTitle());
                intent.putExtra("description", note.getDescription());

                //activityResultLauncher
                activityResultLauncherForUpdateNote.launch(intent);


            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.new_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.top_menu) {
            Intent intent = new Intent(MainActivity.this, AddNoteActivity.class);
//            startActivityForResult(intent, 1);
            activityResultLauncherForAddNote.launch(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if(requestCode == 1 && resultCode == RESULT_OK && data != null){
//            String title = data.getStringExtra("noteTitle");
//            String description = data.getStringExtra("noteDescription");
//
//            Note note = new Note(title, description);
//            noteViewModel.insert(note);
//        }
//    }

    public void registerActivityForUpdateNote(){

        activityResultLauncherForUpdateNote = registerForActivityResult(new ActivityResultContracts.StartActivityForResult()
                , new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {

                        int resultCode = result.getResultCode();
                        Intent data = result.getData();
                        if(resultCode == RESULT_OK && data != null){
                            String title = data.getStringExtra("titleLast");
                            String description = data.getStringExtra("descriptionLast");
                            int id = data.getIntExtra("noteId", -1);

                            Note note = new Note(title, description);
                            note.setId(id);

                            noteViewModel.update(note);
                        }

                    }
                });
    }

    public void registerActivityForAddNote() {
        activityResultLauncherForAddNote = registerForActivityResult(new ActivityResultContracts.StartActivityForResult()
                , new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {

                        int resultCode = result.getResultCode();
                        Intent data = result.getData();

                        if (resultCode == RESULT_OK && data != null) {
                            String title = data.getStringExtra("noteTitle");
                            String description = data.getStringExtra("noteDescription");

                            Note note = new Note(title, description);
                            noteViewModel.insert(note);
                        }

                    }
                });
    }
}