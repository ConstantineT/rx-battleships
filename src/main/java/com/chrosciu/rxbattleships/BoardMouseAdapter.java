package com.chrosciu.rxbattleships;

import reactor.core.publisher.Flux;

public interface BoardMouseAdapter {
    Flux<Shot> getShotFlux();
}
