package com.example.demo.Controller;

import com.example.demo.Entity.Member;
import com.example.demo.Service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/members")
@CrossOrigin(origins = {"http://localhost:8080", "http://127.0.0.1:5500"}) // Allow requests from your frontend origins
public class MemberController {

    @Autowired
    private MemberService memberService;

    /**
     * POST /api/members/register
     * Registers a new member.
     * @param member The member object to register (email, password, name are required).
     * @return The registered member with HTTP status 201 Created, or 409 Conflict if email exists.
     */
    @PostMapping("/register")
    public ResponseEntity<Member> registerMember(@RequestBody Member member) {
        Member registeredMember = memberService.registerMember(member);
        if (registeredMember == null) {
            return new ResponseEntity<>(HttpStatus.CONFLICT); // Email already exists
        }
        // Do not return the password in the response for security reasons
        registeredMember.setPassword(null);
        return new ResponseEntity<>(registeredMember, HttpStatus.CREATED);
    }

    /**
     * POST /api/members/login
     * Authenticates a member.
     * @param credentials A map containing "email" and "password".
     * @return The authenticated member (without password) with HTTP status 200 OK, or 401 Unauthorized.
     */
    @PostMapping("/login")
    public ResponseEntity<Member> loginMember(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        if (email == null || password == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Optional<Member> authenticatedMember = memberService.authenticateMember(email, password);
        if (authenticatedMember.isPresent()) {
            Member member = authenticatedMember.get();
            // Do not return the password in the response for security reasons
            member.setPassword(null);
            return new ResponseEntity<>(member, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    /**
     * GET /api/members
     * Retrieves all members (typically for admin view).
     * @return A list of all members.
     */
    @GetMapping
    public ResponseEntity<List<Member>> getAllMembers() {
        List<Member> members = memberService.getAllMembers();
        // Clear passwords before sending to frontend
        members.forEach(member -> member.setPassword(null));
        return new ResponseEntity<>(members, HttpStatus.OK);
    }

    /**
     * GET /api/members/{id}
     * Retrieves a member by ID.
     * @param id The ID of the member.
     * @return The member if found, or 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Member> getMemberById(@PathVariable Long id) {
        Optional<Member> member = memberService.getMemberById(id);
        return member.map(value -> {
            value.setPassword(null); // Clear password
            return new ResponseEntity<>(value, HttpStatus.OK);
        }).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * PUT /api/members/{id}
     * Updates an existing member's details.
     * @param id The ID of the member to update.
     * @param memberDetails The updated member details.
     * @return The updated member with HTTP status 200 OK, 404 Not Found, or 409 Conflict if email exists.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Member> updateMember(@PathVariable Long id, @RequestBody Member memberDetails) {
        Optional<Member> updatedMember = memberService.updateMember(id, memberDetails);
        if (updatedMember.isPresent()) {
            Member member = updatedMember.get();
            if (member == null) { // This indicates an email conflict from the service
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
            member.setPassword(null); // Clear password
            return new ResponseEntity<>(member, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    /**
     * DELETE /api/members/{id}
     * Deletes a member by ID.
     * @param id The ID of the member to delete.
     * @return HTTP status 204 No Content if deleted, or 404 Not Found.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long id) {
        if (memberService.deleteMember(id)) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // Successfully deleted
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Member not found
    }

    /**
     * PATCH /api/members/{memberId}/lastBorrowDate
     * Updates a member's last borrow date. This would typically be called
     * by a borrowing transaction logic.
     * @param memberId The ID of the member.
     * @param requestBody A map containing "lastBorrowDate" in "YYYY-MM-DD" format.
     * @return The updated member with HTTP status 200 OK, or 404 Not Found.
     */
    @PatchMapping("/{memberId}/lastBorrowDate")
    public ResponseEntity<Member> updateMemberLastBorrowDate(@PathVariable Long memberId, @RequestBody Map<String, String> requestBody) {
        String dateString = requestBody.get("lastBorrowDate");
        if (dateString == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        try {
            LocalDate newDate = LocalDate.parse(dateString);
            Optional<Member> updatedMember = memberService.updateMemberLastBorrowDate(memberId, newDate);
            if (updatedMember.isPresent()) {
                Member member = updatedMember.get();
                member.setPassword(null); // Clear password
                return new ResponseEntity<>(member, HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // Invalid date format
        }
    }
}
