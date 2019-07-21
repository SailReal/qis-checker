package de.albsig.qischecker.data;

import android.content.Context;

import java.io.Serializable;

public class Exam implements Serializable {

    public enum Kind {
        F, G, K, L, MO, MP, MT, S, Z, UNDEFINED
    }

    private static final long serialVersionUID = -4473350889205404637L;
    private int id;
    private String examNo;
    private String name = "-";
    private String semester = "-";
    private String grade = "-";
    private String state = "-";
    private String ects = "-";
    private String kind = "-";
    private String pv = "-";
    private String tryCount = "-";
    private String date = "-";

    public Exam(String examNo) {
        this.setExamNo(examNo);

    }

    public Kind getKindEnum() {
        try {
            return Kind.valueOf(this.getKind());

        } catch (Exception e) {
            return Kind.UNDEFINED;

        }
    }

    private String getStringResource(Context c, int id) {
        return c.getResources().getString(id);

    }

    public int getId() {
        return id;

    }

    public String getExamNo() {
        return examNo;

    }

    public String getName() {
        return name;

    }

    public String getECTS() {
        return ects;

    }

    public String getSemester() {
        return semester;

    }

    public String getKind() {
        return kind;

    }

    public String getPv() {
        return pv;
    }

    public String getTryCount() {
        return tryCount;

    }

    public String getDate() {
        return date;
    }

    public String getGrade() {
        return grade;

    }

    public String getState() {
        return state;

    }

    public Exam setExamNo(String examNo) {
        this.examNo = examNo.replaceAll(" +", " ");
        this.id = examNo.hashCode();
        return this;

    }

    public Exam setName(String name) {
        this.name = name;
        return this;

    }

    public Exam setECTS(String ects) {
        this.ects = ects;
        return this;

    }

    public Exam setSemester(String semester) {
        this.semester = semester;
        this.id = (examNo + this.semester).hashCode();
        return this;

    }

    public Exam setKind(String kind) {
        this.kind = kind;
        return this;

    }

    public Exam setPv(String pv) {
        this.pv = pv;
        return this;
    }

    public Exam setTryCount(String tryCount) {
        this.tryCount = tryCount;
        return this;

    }

    public Exam setDate(String date) {
        this.date = date;
        return this;
    }

    public Exam setGrade(String grade) {
        this.grade = grade;
        return this;

    }

    public Exam setState(String state) {
        this.state = state;
        return this;

    }
}
