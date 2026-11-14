package com.example.studentmanagement.service.impl;

import com.example.studentmanagement.dto.StudentRequest;
import com.example.studentmanagement.dto.StudentResponse;
import com.example.studentmanagement.entity.Student;
import com.example.studentmanagement.exception.DuplicateResourceException;
import com.example.studentmanagement.exception.ResourceNotFoundException;
import com.example.studentmanagement.repository.StudentRepository;
import com.example.studentmanagement.service.StudentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    public StudentServiceImpl(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    @Transactional
    public StudentResponse createStudent(StudentRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (studentRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("A student with email '" + email + "' already exists");
        }

        Student student = new Student();
        applyRequest(student, request, email);
        Student savedStudent = studentRepository.save(student);
        return mapToResponse(savedStudent);
    }

    @Override
    public List<StudentResponse> getAllStudents() {
        return studentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public StudentResponse getStudentById(Long id) {
        Student student = findStudentOrThrow(id);
        return mapToResponse(student);
    }

    @Override
    @Transactional
    public StudentResponse updateStudent(Long id, StudentRequest request) {
        Student student = findStudentOrThrow(id);
        String email = normalizeEmail(request.getEmail());

        if (studentRepository.existsByEmailAndIdNot(email, id)) {
            throw new DuplicateResourceException("A student with email '" + email + "' already exists");
        }

        applyRequest(student, request, email);
        Student updatedStudent = studentRepository.save(student);
        return mapToResponse(updatedStudent);
    }

    @Override
    @Transactional
    public void deleteStudent(Long id) {
        Student student = findStudentOrThrow(id);
        studentRepository.delete(student);
    }

    @Override
    public List<StudentResponse> getStudentsByCourse(String course) {
        return studentRepository.findByCourseIgnoreCase(course.trim())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private Student findStudentOrThrow(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }

    private void applyRequest(Student student, StudentRequest request, String normalizedEmail) {
        student.setName(request.getName().trim());
        student.setEmail(normalizedEmail);
        student.setCourse(request.getCourse().trim());
        student.setAge(request.getAge());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private StudentResponse mapToResponse(Student student) {
        return new StudentResponse(
                student.getId(),
                student.getName(),
                student.getEmail(),
                student.getCourse(),
                student.getAge()
        );
    }
}
