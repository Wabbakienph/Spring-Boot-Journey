package com.example.demo.student;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service // OR @Component but Spring allows us to be more specific
public class StudentService {

    private final StudentRepository studentRepository;

    @Autowired
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public List<Student> getStudents() {
        return studentRepository.findAll();
    }

    public void addNewStudent(Student student) {
        Optional<Student> studentOptional =
                studentRepository.findStudentByEmail(student.getEmail());
        if (studentOptional.isPresent()) {
            throw new IllegalStateException("email taken");
        }
        studentRepository.save(student);
    }

    public void deleteStudent(Long studentId) {
        boolean studentExists = studentRepository.existsById(studentId);
        if (!studentExists) {
            throw new IllegalStateException("Student with " + studentId + " does not exist");
        }
        studentRepository.deleteById(studentId);
    }

    @Transactional
    public void updateStudent(Long studentId, String name, String email, LocalDate dob) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalStateException(
                        "Student with " + studentId + " does not exist"));

        // Thorough check: name present, not null and not the same with current name
        if (name != null &&
                name.length() > 0 &&
                !Objects.equals(student.getName(), name))
            student.setName(name);

        if (email != null &&
                email.isEmpty() &&
                !Objects.equals(student.getEmail(), email)) {
            // CHECK IF EMAIL IS TAKEN in addition to identity checks
            Optional<Student> studentOptional =
                    studentRepository.findStudentByEmail(student.getEmail());
            if (studentOptional.isPresent()) {
                throw new IllegalStateException("email taken");
            }
            student.setEmail(email);
        }

        // Thorough check for dob: non-null, reasonable date range (18+ - 124), and not the same as current dob
        if (dob != null &&
                !dob.isAfter(LocalDate.of(2006,12,31)) &&
                !dob.isBefore(LocalDate.of(1900,1,1)) &&
                !Objects.equals(student.getDob(), dob)
        )
            student.setDob(dob);

        studentRepository.save(student);
    }
}
