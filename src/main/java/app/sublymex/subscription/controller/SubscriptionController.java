package app.sublymex.subscription.controller;

import app.sublymex.subscription.dto.CreateSubscriptionRequest;
import app.sublymex.subscription.dto.SubscriptionResponse;
import app.sublymex.subscription.dto.UpdateSubscriptionRequest;
import app.sublymex.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final String USER_ID_HEADER = "X-USER-ID";

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    // TODO: Replace Header X-User-Id with authenticated user
    @GetMapping
    public List<SubscriptionResponse> getSubscriptions(@RequestHeader(USER_ID_HEADER) UUID userId) {
        return subscriptionService.list(userId);
    }

    // TODO: Replace Header X-User-Id with authenticated user
    @GetMapping("/{id}")
    public SubscriptionResponse getById(@PathVariable UUID id, @RequestHeader(USER_ID_HEADER) UUID userId) {
        return subscriptionService.getById(id, userId);
    }

    // TODO: Replace Header X-User-Id with authenticated user
    @PostMapping
    public ResponseEntity<SubscriptionResponse> create(
            @RequestBody @Valid CreateSubscriptionRequest subscriptionRequest, @RequestHeader(USER_ID_HEADER) UUID userId
    ) {
        SubscriptionResponse subscriptionResponse = subscriptionService.create(subscriptionRequest, userId);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(subscriptionResponse.id())
                .toUri();

        return ResponseEntity.created(location).body(subscriptionResponse);
    }

    // TODO: Replace Header X-User-Id with authenticated user
    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> update(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateSubscriptionRequest subscriptionRequest,
            @RequestHeader(USER_ID_HEADER) UUID userId
    ) {
        SubscriptionResponse subscriptionResponse = subscriptionService.update(subscriptionRequest, id, userId);

        return ResponseEntity.ok(subscriptionResponse);
    }

    // TODO: Replace Header X-User-Id with authenticated user
    @DeleteMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> delete(@PathVariable UUID id, @RequestHeader(USER_ID_HEADER) UUID userId) {
        subscriptionService.delete(id, userId);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
