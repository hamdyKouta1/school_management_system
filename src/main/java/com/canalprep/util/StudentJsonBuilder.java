package com.canalprep.util;

import com.canalprep.model.Notes;
import com.canalprep.model.ParentDetails;
import com.canalprep.model.Student;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class StudentJsonBuilder {

    public static Student buildStudentFromJson(JSONObject json) throws JSONException {
        Student student = new Student();
        student.setStudentName(json.getString("student_name"));
        student.setNid(json.getString("nid"));
        student.setNationalityName(json.getString("nationality"));
        student.setReligionName(json.getString("religion"));
        student.setCurrentAddress(json.getString("current_address"));
        student.setMedicalStatus(json.getBoolean("medical_status"));

        // Parse date
        String dobString = json.optString("date_of_birth");
        if (dobString != null && !dobString.isEmpty()) {
            student.setDateOfBirth(Date.valueOf(dobString));
        }

        student.setPlaceOfBirth(json.optString("place_of_birth"));
        student.setGradeName(json.getString("grade"));
        student.setClassName(json.getString("class"));

        // Medical descriptions
        if (json.has("medical_descriptions")) {
            student.setMedicalDescriptions(json.getString("medical_descriptions"));
        }

        // Student phones
        if (json.has("student_phones")) {
            JSONArray phoneArray = json.getJSONArray("student_phones");
            List<String> phones = new ArrayList<>();
            for (int i = 0; i < phoneArray.length(); i++) {
                phones.add(phoneArray.getString(i));
            }
            student.setStudentPhones(phones);
        }

        // Student notes
        if (json.has("student_notes")) {
            JSONArray notesArray = json.getJSONArray("student_notes");
            List<Notes> notesList = new ArrayList<>();
            for (int i = 0; i < notesArray.length(); i++) {
                JSONObject noteObj = notesArray.getJSONObject(i);
                Notes note = new Notes();
                note.setNoteText(noteObj.getString("note_text"));
                note.setCreatedBy(noteObj.getString("created_by"));
                notesList.add(note);
            }
            student.setStudentNotes(notesList);
        }

        // Parents info
        if (json.has("parents_info")) {
            JSONArray parentsArray = json.getJSONArray("parents_info");
            List<ParentDetails> parents = new ArrayList<>();
            for (int i = 0; i < parentsArray.length(); i++) {
                JSONObject parentObj = parentsArray.getJSONObject(i);
                ParentDetails parent = new ParentDetails();
                parent.setParentName(parentObj.getString("parent_name"));
                parent.setRelationship(parentObj.getString("relationship"));
                parent.setParentNid(parentObj.getString("parent_nid"));
                parent.setParentNationality(parentObj.getString("parent_nationality"));
                parent.setParentJob(parentObj.getString("parent_job"));
                parent.setParentAddress(parentObj.getString("parent_address"));
                parent.setParentSocialStatus(parentObj.getString("parent_social_status"));

                if (parentObj.has("parent_phones")) {
                    JSONArray parentPhones = parentObj.getJSONArray("parent_phones");
                    List<String> parentPhoneList = new ArrayList<>();
                    for (int j = 0; j < parentPhones.length(); j++) {
                        parentPhoneList.add(parentPhones.getString(j));
                    }
                    parent.setParentPhones(parentPhoneList);
                }
                parents.add(parent);
            }
            student.setParentsInfo(parents);
        }

        return student;
    }
}


