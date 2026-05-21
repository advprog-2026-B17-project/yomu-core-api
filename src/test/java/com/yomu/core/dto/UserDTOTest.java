package com.yomu.core.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserDTOTest {

    @Test
    @DisplayName("6-arg constructor sets all fields correctly")
    void sixArgConstructorSetsAllFields() {
        UserDTO dto = new UserDTO(
                "id-1", "alice", "alice@example.com",
                "+62812345678", "Alice", "student"
        );

        assertEquals("id-1", dto.getId());
        assertEquals("alice", dto.getUsername());
        assertEquals("alice@example.com", dto.getEmail());
        assertEquals("+62812345678", dto.getPhone());
        assertEquals("Alice", dto.getDisplayName());
        assertEquals("student", dto.getRole());
    }

    @Test
    @DisplayName("5-arg constructor sets fields with phone as null")
    void fiveArgConstructorSetsFieldsWithPhoneNull() {
        UserDTO dto = new UserDTO(
                "id-1", "alice", "alice@example.com",
                "Alice", "student"
        );

        assertEquals("id-1", dto.getId());
        assertEquals("alice", dto.getUsername());
        assertEquals("alice@example.com", dto.getEmail());
        assertNull(dto.getPhone());
        assertEquals("Alice", dto.getDisplayName());
        assertEquals("student", dto.getRole());
    }

    @Test
    @DisplayName("default constructor creates empty DTO")
    void defaultConstructorCreatesEmptyDTO() {
        UserDTO dto = new UserDTO();

        assertNull(dto.getId());
        assertNull(dto.getUsername());
        assertNull(dto.getEmail());
        assertNull(dto.getPhone());
        assertNull(dto.getDisplayName());
        assertNull(dto.getRole());
    }

    @Test
    @DisplayName("setters update fields correctly")
    void settersUpdateFieldsCorrectly() {
        UserDTO dto = new UserDTO();

        dto.setId("id-1");
        dto.setUsername("alice");
        dto.setEmail("alice@example.com");
        dto.setPhone("+62812345678");
        dto.setDisplayName("Alice");
        dto.setRole("student");

        assertEquals("id-1", dto.getId());
        assertEquals("alice", dto.getUsername());
        assertEquals("alice@example.com", dto.getEmail());
        assertEquals("+62812345678", dto.getPhone());
        assertEquals("Alice", dto.getDisplayName());
        assertEquals("student", dto.getRole());
    }

    @Test
    @DisplayName("setPhone to null works")
    void setPhoneToNullWorks() {
        UserDTO dto = new UserDTO("id", "user", "email@test.com", "+62800", "Name", "role");

        dto.setPhone(null);

        assertNull(dto.getPhone());
    }

    @Test
    @DisplayName("phone can be updated from value to different value")
    void phoneCanBeUpdated() {
        UserDTO dto = new UserDTO();
        dto.setPhone("+62812345678");

        dto.setPhone("+628987654321");

        assertEquals("+628987654321", dto.getPhone());
    }
}
