//package com.example.diplomathesisfinalcollection.team;
//
//
////import com.example.medicalweb.department.Department;
////import com.example.medicalweb.doctoruser.DoctorUser;
//import com.example.diplomathesisfinalcollection.department.Department;
//import com.example.diplomathesisfinalcollection.doctoruser.DoctorUser;
//import org.springframework.data.annotation.Id;
//import org.springframework.data.mongodb.core.mapping.DBRef;
//import org.springframework.data.mongodb.core.mapping.Document;
//import org.springframework.data.mongodb.core.mapping.DocumentReference;
//
//@Document(collection = "team")
//public class Team {
//    @Id
//    private String id;
//    private String name;
//    private String surname;
//    private int age;
//    private String description;
//    private String phone;
//    private String email;
//    private String photoUrl;
//
//    @DBRef
//    private Department department;
//    private String departmentName;
//
//    @DocumentReference
//    private DoctorUser userAccount;
//
//
//    public Team() {
//    }
//
//    public Team(String id, String name, String surname, int age, String description, String phone, String email, String photoUrl, String departmentName, Department department, DoctorUser userAccount) {
//        this.id = id;
//        this.name = name;
//        this.surname = surname;
//        this.age = age;
//        this.description = description;
//        this.phone = phone;
//        this.email = email;
//        this.photoUrl = photoUrl;
//        this.departmentName = departmentName;
//        this.department = department;
//        this.userAccount = userAccount;
//    }
//
//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public String getSurname() {
//        return surname;
//    }
//
//    public void setSurname(String surname) {
//        this.surname = surname;
//    }
//
//    public int getAge() {
//        return age;
//    }
//
//    public void setAge(int age) {
//        this.age = age;
//    }
//
//    public Department getDepartment() {
//        return department;
//    }
//
//    public void setDepartment(Department department) {
//        this.department = department;
//    }
//
//    public String getDepartmentName() {
//        return departmentName;
//    }
//
//
//    public String getDescription() {
//        return description;
//    }
//
//    public void setDescription(String description) {
//        this.description = description;
//    }
//
//    public String getPhone() {
//        return phone;
//    }
//
//    public void setPhone(String phone) {
//        this.phone = phone;
//    }
//
//    public String getEmail() {
//        return email;
//    }
//
//    public void setEmail(String email) {
//        this.email = email;
//    }
//
//    public String getPhotoUrl() {
//        return photoUrl;
//    }
//
//    public void setPhotoUrl(String photoUrl) {
//        this.photoUrl = photoUrl;
//    }
//
//    public void setDepartmentName(String departmentName) {
//        this.departmentName = departmentName;
//    }
//
//    public DoctorUser getDoctorUserAccount() {
//        return userAccount;
//    }
//
//    public void setDoctorUserAccount(DoctorUser userAccount) {
//        this.userAccount = userAccount;
//    }
//
//}
