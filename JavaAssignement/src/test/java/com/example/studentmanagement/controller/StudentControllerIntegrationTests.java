package com.example.studentmanagement.controller;

import com.example.studentmanagement.dto.StudentRequest;
import com.example.studentmanagement.repository.StudentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StudentControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StudentRepository studentRepository;

    @BeforeEach
    void cleanDatabase() {
        studentRepository.deleteAll();
    }

    @Test
    void createStudent_returnsCreatedWithLocationHeader() throws Exception {
        StudentRequest request = new StudentRequest("Alice Johnson", "alice@example.com", "Computer Science", 20);

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Alice Johnson"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.course").value("Computer Science"))
                .andExpect(jsonPath("$.age").value(20));
    }

    @Test
    void createStudent_withDuplicateEmail_returnsConflict() throws Exception {
        StudentRequest request = new StudentRequest("Alice Johnson", "alice@example.com", "Computer Science", 20);

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void createStudent_withInvalidBody_returnsBadRequestWithFieldErrors() throws Exception {
        StudentRequest request = new StudentRequest("", "not-an-email", "", 0);

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.name").exists())
                .andExpect(jsonPath("$.validationErrors.email").exists())
                .andExpect(jsonPath("$.validationErrors.course").exists())
                .andExpect(jsonPath("$.validationErrors.age").exists());
    }

    @Test
    void getStudentById_whenMissing_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/students/{id}", 9999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Student not found with id: 9999"));
    }

    @Test
    void getAllStudents_returnsAllStudents() throws Exception {
        createStudent("Alice Johnson", "alice@example.com", "Computer Science", 20);
        createStudent("Bob Smith", "bob@example.com", "Mathematics", 23);

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getStudentsByCourse_isCaseInsensitive() throws Exception {
        createStudent("Alice Johnson", "alice@example.com", "Computer Science", 20);
        createStudent("Bob Smith", "bob@example.com", "Mathematics", 23);
        createStudent("Carol White", "carol@example.com", "Computer Science", 21);

        mockMvc.perform(get("/api/students/course/{course}", "computer science"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].course").value("Computer Science"));
    }

    @Test
    void updateStudent_returnsUpdatedStudent() throws Exception {
        long id = createStudent("Alice Johnson", "alice@example.com", "Computer Science", 20);

        StudentRequest update = new StudentRequest("Alice Johnson", "alice.johnson@example.com", "Data Science", 21);

        mockMvc.perform(put("/api/students/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("alice.johnson@example.com"))
                .andExpect(jsonPath("$.course").value("Data Science"))
                .andExpect(jsonPath("$.age").value(21));
    }

    @Test
    void deleteStudent_removesStudentAndReturnsNoContent() throws Exception {
        long id = createStudent("Alice Johnson", "alice@example.com", "Computer Science", 20);

        mockMvc.perform(delete("/api/students/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/students/{id}", id))
                .andExpect(status().isNotFound());
    }

    private long createStudent(String name, String email, String course, int age) throws Exception {
        StudentRequest request = new StudentRequest(name, email, course, age);

        String response = mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }
}
