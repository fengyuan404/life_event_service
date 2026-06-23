package com.lifeevent.service.controller;

import com.lifeevent.service.dto.*;
import com.lifeevent.service.entity.*;
import com.lifeevent.service.service.BusinessService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BusinessController {
    private final BusinessService businessService;

    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    @GetMapping("/families")
    public ApiResponse<PageResult<Family>> families(@RequestParam(required = false) String keyword,
                                                    @RequestParam(defaultValue = "1") long page,
                                                    @RequestParam(defaultValue = "10") long size) {
        return ApiResponse.ok(businessService.families(keyword, page, size));
    }

    @PostMapping("/families")
    public ApiResponse<Void> createFamily(@Valid @RequestBody Family family) {
        businessService.saveFamily(family);
        return ApiResponse.ok(null);
    }

    @PutMapping("/families/{id}")
    public ApiResponse<Void> updateFamily(@PathVariable Integer id, @Valid @RequestBody Family family) {
        family.setFamilyId(id);
        businessService.saveFamily(family);
        return ApiResponse.ok(null);
    }

    @DeleteMapping("/families/{id}")
    public ApiResponse<Void> deleteFamily(@PathVariable Integer id) {
        businessService.deleteFamily(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/deceased")
    public ApiResponse<PageResult<Map<String, Object>>> deceased(@RequestParam(required = false) String keyword,
                                                                 @RequestParam(defaultValue = "1") long page,
                                                                 @RequestParam(defaultValue = "10") long size) {
        return ApiResponse.ok(businessService.deceased(keyword, page, size));
    }

    @PostMapping("/deceased")
    public ApiResponse<Void> createDeceased(@Valid @RequestBody Deceased deceased) {
        businessService.saveDeceased(deceased);
        return ApiResponse.ok(null);
    }

    @PutMapping("/deceased/{id}")
    public ApiResponse<Void> updateDeceased(@PathVariable Integer id, @Valid @RequestBody Deceased deceased) {
        deceased.setDeceasedId(id);
        businessService.saveDeceased(deceased);
        return ApiResponse.ok(null);
    }

    @GetMapping("/grave-areas")
    public ApiResponse<PageResult<GraveArea>> graveAreas(@RequestParam(required = false) String keyword,
                                                         @RequestParam(defaultValue = "1") long page,
                                                         @RequestParam(defaultValue = "10") long size) {
        return ApiResponse.ok(businessService.graveAreas(keyword, page, size));
    }

    @PostMapping("/grave-areas")
    public ApiResponse<Void> createGraveArea(@Valid @RequestBody GraveArea area) {
        businessService.saveGraveArea(area);
        return ApiResponse.ok(null);
    }

    @PutMapping("/grave-areas/{id}")
    public ApiResponse<Void> updateGraveArea(@PathVariable Integer id, @Valid @RequestBody GraveArea area) {
        area.setAreaId(id);
        businessService.saveGraveArea(area);
        return ApiResponse.ok(null);
    }

    @GetMapping("/graves")
    public ApiResponse<PageResult<Map<String, Object>>> graves(@RequestParam(required = false) String keyword,
                                                               @RequestParam(defaultValue = "1") long page,
                                                               @RequestParam(defaultValue = "10") long size) {
        return ApiResponse.ok(businessService.graves(keyword, page, size));
    }

    @PostMapping("/graves")
    public ApiResponse<Void> createGrave(@Valid @RequestBody Grave grave) {
        businessService.saveGrave(grave);
        return ApiResponse.ok(null);
    }

    @PutMapping("/graves/{id}")
    public ApiResponse<Void> updateGrave(@PathVariable Integer id, @Valid @RequestBody Grave grave) {
        grave.setGraveId(id);
        businessService.saveGrave(grave);
        return ApiResponse.ok(null);
    }

    @GetMapping("/graves/empty")
    public ApiResponse<List<Map<String, Object>>> emptyGraves() {
        return ApiResponse.ok(businessService.emptyGraves());
    }

    @GetMapping("/rents")
    public ApiResponse<PageResult<Map<String, Object>>> rents(@RequestParam(required = false) String keyword,
                                                              @RequestParam(defaultValue = "1") long page,
                                                              @RequestParam(defaultValue = "10") long size) {
        return ApiResponse.ok(businessService.rents(keyword, page, size));
    }

    @PostMapping("/rents")
    public ApiResponse<Void> createRent(@Valid @RequestBody RentCreateRequest request) {
        businessService.createRent(request);
        return ApiResponse.ok(null);
    }

    @PutMapping("/rents/{id}")
    public ApiResponse<Void> updateRent(@PathVariable Integer id, @RequestBody RentRecord rent) {
        businessService.updateRent(id, rent);
        return ApiResponse.ok(null);
    }

    @GetMapping("/rents/expiring")
    public ApiResponse<List<Map<String, Object>>> expiringRents() {
        return ApiResponse.ok(businessService.expiringRents());
    }

    @GetMapping("/payments")
    public ApiResponse<PageResult<Map<String, Object>>> payments(@RequestParam(required = false) String keyword,
                                                                 @RequestParam(defaultValue = "1") long page,
                                                                 @RequestParam(defaultValue = "10") long size) {
        return ApiResponse.ok(businessService.payments(keyword, page, size));
    }

    @PostMapping("/payments")
    public ApiResponse<Void> createPayment(@Valid @RequestBody PaymentCreateRequest request) {
        businessService.createPayment(request);
        return ApiResponse.ok(null);
    }

    @GetMapping("/sacrifice-books")
    public ApiResponse<PageResult<Map<String, Object>>> sacrificeBooks(@RequestParam(required = false) String keyword,
                                                                       @RequestParam(defaultValue = "1") long page,
                                                                       @RequestParam(defaultValue = "10") long size) {
        return ApiResponse.ok(businessService.sacrificeBooks(keyword, page, size));
    }

    @PostMapping("/sacrifice-books")
    public ApiResponse<Void> createBook(@Valid @RequestBody BookCreateRequest request) {
        businessService.createBook(request);
        return ApiResponse.ok(null);
    }

    @PutMapping("/sacrifice-books/{id}/checkin")
    public ApiResponse<Void> checkinBook(@PathVariable Integer id) {
        businessService.checkinBook(id);
        return ApiResponse.ok(null);
    }

    @PutMapping("/sacrifice-books/{id}/cancel")
    public ApiResponse<Void> cancelBook(@PathVariable Integer id) {
        businessService.cancelBook(id);
        return ApiResponse.ok(null);
    }

    @GetMapping("/staff")
    public ApiResponse<List<Staff>> staff() {
        return ApiResponse.ok(businessService.staff());
    }
}

