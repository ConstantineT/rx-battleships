package com.chrosciu.rxbattleships.service;

import com.chrosciu.rxbattleships.model.Field;
import com.chrosciu.rxbattleships.model.Ship;
import com.chrosciu.rxbattleships.model.ShotResult;
import com.chrosciu.rxbattleships.model.Stamp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BattleServiceImpl implements BattleService {

    private final List<Ship> ships = new ArrayList<>();

    private final ShipPositionFluxService shipPositionFluxService;
    private final FieldFluxService fieldFluxService;

    @Override
    public Mono<Void> getShipsReadyMono() {
        return shipPositionFluxService
                .getShipPositionFlux()
                .map(Ship::new)
                .collectList()
                .doOnNext(ships::addAll)
                .then();
    }

    @Override
    public Flux<Stamp> getStampFlux() {
        return fieldFluxService.getFieldFlux()
                .flatMap(field -> Flux.fromIterable(ships)
                        .map(ship -> Tuples.of(ship, ship.takeShot(field)))
                        .takeUntil(tuple -> ShotResult.MISSED != tuple.getT2()) // take until the very first hit or sunk
                        .takeLast(1) // leave only the last result for each particular field
                        .map(tuple -> buildStamps(field, tuple.getT1(), tuple.getT2())))
                .takeUntil(stamps -> ships.stream().allMatch(Ship::isSunk))
                .flatMap(Flux::fromIterable);
    }

    private List<Stamp> buildStamps(final Field field, final Ship ship, final ShotResult result) {
        if (ShotResult.SUNK.equals(result)) {
            return ship.getAllFields().stream()
                    .map(shipField -> new Stamp(shipField, result))
                    .collect(Collectors.toList());
        } else {
            return Collections.singletonList(new Stamp(field, result));
        }
    }
}