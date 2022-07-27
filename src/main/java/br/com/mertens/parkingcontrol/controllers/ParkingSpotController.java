package br.com.mertens.parkingcontrol.controllers;

import br.com.mertens.parkingcontrol.dtos.ParkingSpotDTO;
import br.com.mertens.parkingcontrol.models.ParkingSpotModel;
import br.com.mertens.parkingcontrol.services.ParkingSpotService;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/parking-spot")
public class ParkingSpotController {

    final ParkingSpotService service;

    public ParkingSpotController(ParkingSpotService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity saveParkingSpot(@RequestBody @Valid ParkingSpotDTO dto){

        if(service.existsByLicensePlateCar(dto.getLicensePlateCar())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: License plate Car is already in use!");
        }

        if(service.existsByParkingSpotNumber(dto.getParkingSpotNumber())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: Parking Spot is already in use!");
        }

        if(service.existsByApartmentAndBlock(dto.getApartament(), dto.getBlock())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Conflict: There is already a spot registered do this Apartament/Block!");
        }

        ParkingSpotModel model = new ParkingSpotModel();
        BeanUtils.copyProperties(dto, model);
        model.setRegistrationDate(LocalDateTime.now(ZoneId.of("UTC")));
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(model));
    }

    @GetMapping
    public ResponseEntity<Page<ParkingSpotModel>> getAllParkingSpots(@PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable){
        return ResponseEntity.status(HttpStatus.OK).body(service.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getOneParkingSpot(@PathVariable(value = "id") UUID id){
        Optional<ParkingSpotModel> spotOptional = service.findById(id);
        if(spotOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.OK).body(spotOptional.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found!");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteOneParkingSpot(@PathVariable(value = "id") UUID id){
        Optional<ParkingSpotModel> spotOptional = service.findById(id);
        if(spotOptional.isPresent()) {
            service.delete(spotOptional.get());
            return ResponseEntity.status(HttpStatus.OK).body("Spot removed successfully!");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found!");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateOneParkingSpot(@RequestBody @Valid ParkingSpotDTO dto, @PathVariable(value = "id") UUID id){
        Optional<ParkingSpotModel> spotOptional = service.findById(id);
        if(spotOptional.isPresent()) {
            ParkingSpotModel fromDb = spotOptional.get();
            BeanUtils.copyProperties(dto, fromDb);
            service.save(fromDb);
            return ResponseEntity.status(HttpStatus.OK).body(spotOptional.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Parking Spot not found!");
        }
    }
}
