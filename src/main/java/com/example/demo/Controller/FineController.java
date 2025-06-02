package com.example.demo.Controller;

import com.example.demo.Entity.Fine;
import com.example.demo.Service.FineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/fines")
@CrossOrigin(origins = {"http://localhost:8080", "http://127.0.0.1:5500"}) // Allow requests from your frontend origins
public class FineController {

    @Autowired
    private FineService fineService;

    /**
     * GET /api/fines/member/{memberId}
     * Retrieves all fines for a specific member.
     * @param memberId The ID of the member.
     * @return A list of Fine objects.
     */
    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<Fine>> getFinesByMember(@PathVariable Long memberId) {
        List<Fine> fines = fineService.getFinesByMemberId(memberId);
        return new ResponseEntity<>(fines, HttpStatus.OK);
    }

    /**
     * GET /api/fines/member/{memberId}/pending
     * Retrieves all pending fines for a specific member.
     * @param memberId The ID of the member.
     * @return A list of pending Fine objects.
     */
    @GetMapping("/member/{memberId}/pending")
    public ResponseEntity<List<Fine>> getPendingFinesByMember(@PathVariable Long memberId) {
        List<Fine> pendingFines = fineService.getPendingFinesByMemberId(memberId);
        return new ResponseEntity<>(pendingFines, HttpStatus.OK);
    }

    /**
     * POST /api/fines/pay/{fineId}
     * Marks a specific fine as paid.
     * @param fineId The ID of the fine to mark as paid.
     * @return The updated Fine object with HTTP status 200 OK, or 404 Not Found/400 Bad Request.
     */
    @PostMapping("/pay/{fineId}")
    public ResponseEntity<Fine> payFine(@PathVariable Long fineId) {
        Optional<Fine> updatedFine = fineService.payFine(fineId);
        if (updatedFine.isPresent()) {
            return new ResponseEntity<>(updatedFine.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // Could be not found or already paid
    }

    /**
     * POST /api/fines/payByTransaction/{transactionId}
     * Marks a fine as paid using the associated borrowing transaction ID.
     * @param transactionId The ID of the borrowing transaction.
     * @return The updated Fine object with HTTP status 200 OK, or 404 Not Found/400 Bad Request.
     */
    @PostMapping("/payByTransaction/{transactionId}")
    public ResponseEntity<Fine> payFineByBorrowingTransaction(@PathVariable Long transactionId) {
        Optional<Fine> updatedFine = fineService.payFineByBorrowingTransactionId(transactionId);
        if (updatedFine.isPresent()) {
            return new ResponseEntity<>(updatedFine.get(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST); // Could be not found or already paid
    }
}
