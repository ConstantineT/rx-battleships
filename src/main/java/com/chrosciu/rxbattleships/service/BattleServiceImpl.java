package com.chrosciu.rxbattleships.service;

import com.chrosciu.rxbattleships.config.Constants;
import com.chrosciu.rxbattleships.model.Ship;
import com.chrosciu.rxbattleships.model.ShipWithHits;
import com.chrosciu.rxbattleships.model.Shot;
import com.chrosciu.rxbattleships.model.ShotResult;
import com.chrosciu.rxbattleships.model.ShotWithResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class BattleServiceImpl implements BattleService {
    private final ShipFluxService shipFluxService;
    private final ShotFluxService shotFluxService;

    private boolean[][] shots = new boolean[Constants.BOARD_SIZE][Constants.BOARD_SIZE];
    private List<ShipWithHits> shipsWithHits = new ArrayList<>();

    @Getter
    private Mono<Void> battleReadyMono;

    @Getter
    private Flux<ShotWithResult> shotResultFlux;

    @PostConstruct
    private void init() {
        battleReadyMono = shipFluxService.getShipFlux()
                .doOnNext(this::insertShip).then();
        shotResultFlux = shotFluxService.getShotFlux()
                .filter(this::noShotAlreadyHere)
                .doOnNext(this::markShot)
                .flatMap(this::getShotResultsAfterShot)
                .takeUntil(this::allSunk);
    }

    private Flux<ShotWithResult> getShotResultsAfterShot(Shot shot) {
        Optional<ShipWithHits> shipHitsOptional = shipsWithHits.stream().filter(shipWithHits -> shipWithHits.isHit(shot)).findFirst();
        List<ShotWithResult> shotWithResults = shipHitsOptional.map(shipWithHits -> shipWithHits.takeShot(shot))
                .orElse(Collections.singletonList(ShotWithResult.of(shot, ShotResult.MISSED)));
        return Flux.fromIterable(shotWithResults);
    }

    private void insertShip(Ship ship) {
        shipsWithHits.add(new ShipWithHits(ship));
    }

    private boolean noShotAlreadyHere(Shot shot) {
        return !shots[shot.x][shot.y];
    }

    private void markShot(Shot shot) {
        shots[shot.x][shot.y] = true;
    }

    private boolean allSunk(ShotWithResult shotWithResult) {
        return shipsWithHits.stream().allMatch(ShipWithHits::isSunk);
    }
}
