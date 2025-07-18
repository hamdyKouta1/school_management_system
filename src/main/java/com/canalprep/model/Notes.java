package com.canalprep.model;

import com.fasterxml.jackson.annotation.JsonAlias;

public class Notes {
    @JsonAlias("note_id")
    private int noteId;
    @JsonAlias("note_text")
    private String noteText;
    @JsonAlias("created_by")
    private String createdBy;
    @JsonAlias("note_date")  
    private java.sql.Timestamp noteDate;

    public int getNoteId() {
        return noteId;
    }

    public void setNoteId(int noteId) {
        this.noteId = noteId;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setNoteText(String noteText) {
        this.noteText = noteText;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public java.sql.Timestamp getnoteDate() {
        return noteDate;
    }

    public void setnoteDate(java.sql.Timestamp noteDate) {
        this.noteDate = noteDate;
    }
}
