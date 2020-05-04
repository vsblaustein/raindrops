package com.valentina.game;

import com.badlogic.gdx.math.Rectangle;

public class Raindrop {
    Rectangle raindrop; //shape
    float speed; //speed
    boolean good; //determines which texture is drawn to object

    //constructor
    public Raindrop(int fall){
        this.raindrop = new Rectangle();
        raindrop.width = 64;
        raindrop.height = 64;
        raindrop.x = (int) (Math.random() * 800);
        raindrop.y = 480;
        this.speed = fall;
        this.good = true;
    }


}
