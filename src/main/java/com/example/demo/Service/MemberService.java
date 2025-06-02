package com.example.demo.Service;

import com.example.demo.Entity.Member;
import com.example.demo.Repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * Registers a new member.
     * @param member The member object to register.
     * @return The registered member, or null if email already exists.
     */
    @Transactional
    public Member registerMember(Member member) {
        if (memberRepository.existsByEmail(member.getEmail())) {
            return null; // Member with this email already exists
        }
        member.setPassword(passwordEncoder.encode(member.getPassword()));
        member.setMembershipStatus("ACTIVE"); // Default status for new members
        member.setRole("USER"); // Default role for new members
        member.setLastBorrowDate(LocalDate.now()); // Set initial last borrow date to now
        return memberRepository.save(member);
    }

    /**
     * Authenticates a member.
     * @param email The member's email.
     * @param password The raw password.
     * @return An Optional containing the authenticated member, or empty if authentication fails.
     */
    public Optional<Member> authenticateMember(String email, String password) {
        Optional<Member> memberOptional = memberRepository.findByEmail(email);
        if (memberOptional.isPresent()) {
            Member member = memberOptional.get();
            if (passwordEncoder.matches(password, member.getPassword())) {
                return Optional.of(member);
            }
        }
        return Optional.empty();
    }

    /**
     * Retrieves all members.
     * @return A list of all members.
     */
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    /**
     * Retrieves a member by ID.
     * @param id The ID of the member.
     * @return An Optional containing the member if found.
     */
    public Optional<Member> getMemberById(Long id) {
        return memberRepository.findById(id);
    }

    /**
     * Updates an existing member's details.
     * @param id The ID of the member to update.
     * @param memberDetails The updated member details.
     * @return An Optional containing the updated member, or empty if not found.
     */
    @Transactional
    public Optional<Member> updateMember(Long id, Member memberDetails) {
        return memberRepository.findById(id)
                .map(existingMember -> {
                    existingMember.setName(memberDetails.getName());
                    // Only update email if it's not already taken by another member and it's changed
                    if (!existingMember.getEmail().equals(memberDetails.getEmail()) && memberRepository.existsByEmail(memberDetails.getEmail())) {
                        return null; // Signal conflict
                    }
                    existingMember.setEmail(memberDetails.getEmail());
                    existingMember.setPhone(memberDetails.getPhone());
                    existingMember.setAddress(memberDetails.getAddress());
                    existingMember.setMembershipStatus(memberDetails.getMembershipStatus());
                    // Password update should be handled carefully, allowing update if provided
                    if (memberDetails.getPassword() != null && !memberDetails.getPassword().isEmpty()) {
                        existingMember.setPassword(passwordEncoder.encode(memberDetails.getPassword()));
                    }
                    existingMember.setRole(memberDetails.getRole());
                    // Ensure lastBorrowDate is updated if provided, otherwise retain existing
                    if (memberDetails.getLastBorrowDate() != null) {
                         existingMember.setLastBorrowDate(memberDetails.getLastBorrowDate());
                    }
                    return memberRepository.save(existingMember);
                });
    }

    /**
     * Deletes a member by ID.
     * @param id The ID of the member to delete.
     * @return True if deleted, false otherwise.
     */
    public boolean deleteMember(Long id) {
        if (memberRepository.existsById(id)) {
            memberRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Updates a member's last borrow date. This method would be called by the
     * Book Borrowing and Return module when a member borrows a book.
     * @param memberId The ID of the member.
     * @param newDate The new last borrow date.
     * @return An Optional containing the updated member, or empty if not found.
     */
    @Transactional
    public Optional<Member> updateMemberLastBorrowDate(Long memberId, LocalDate newDate) {
        return memberRepository.findById(memberId)
                .map(member -> {
                    member.setLastBorrowDate(newDate);
                    return memberRepository.save(member);
                });
    }

    /**
     * Scheduled task to update member status to INACTIVE if they haven't borrowed
     * a book for a specified period (e.g., 3 months).
     * This task runs daily at midnight (00:00).
     */
    @Scheduled(cron = "0 0 0 * * ?") // Runs every day at midnight (seconds, minutes, hours, day-of-month, month, day-of-week)
    @Transactional
    public void updateInactiveMembers() {
        // Define the inactivity period (e.g., 3 months)
        LocalDate threeMonthsAgo = LocalDate.now().minusMonths(3);
        
        List<Member> activeMembers = memberRepository.findAll(); // Fetch all members

        for (Member member : activeMembers) {
            // Check if the member is currently ACTIVE and their last borrow date is before the threshold
            if ("ACTIVE".equalsIgnoreCase(member.getMembershipStatus()) &&
                member.getLastBorrowDate() != null &&
                member.getLastBorrowDate().isBefore(threeMonthsAgo)) {
                
                member.setMembershipStatus("INACTIVE");
                memberRepository.save(member);
                System.out.println("Member " + member.getEmail() + " (ID: " + member.getMemberId() + ") set to INACTIVE due to inactivity.");
            }
        }
    }
}
