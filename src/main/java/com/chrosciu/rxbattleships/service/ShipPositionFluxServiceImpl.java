package com.chrosciu.rxbattleships.service;

import com.chrosciu.rxbattleships.config.Constants;
import com.chrosciu.rxbattleships.model.ShipPosition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class ShipPositionFluxServiceImpl implements ShipPositionFluxService {
    private final ShipPlacementService shipPlacementService;

    @Override
    public Flux<ShipPosition> getShipPositionFlux() {
        return Flux.fromStream(Arrays.stream(Constants.SHIP_SIZES).boxed())
                .subscribeOn(Schedulers.elastic())
                .map(shipPlacementService::placeShip);
    }
}