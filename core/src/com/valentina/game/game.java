package com.valentina.game;

import py4j.GatewayServer;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

public class game implements ApplicationListener, GestureDetector.GestureListener {
	private SpriteBatch batch;
	private Texture img;
	private Texture img2;
	private Texture img3;
	private OrthographicCamera cam;
	private Rectangle bucket;
	private Vector3 pos;
	private GestureDetector gd;
	private Array<Raindrop> raindropArr;
	private BitmapFont font;
	private long lastRaindropTime = 0;
	private int score = 0;
	private int fall = 0;
	private int dropSpeed = 0;
	private boolean canMove = false;
	public GatewayServer gatewayServer;

	@Override
	public void create () {
		//gateway server
		gatewayServer = new GatewayServer(this);
		gatewayServer.start();
		System.out.println("server has started");

		//orthographic camera
		cam = new OrthographicCamera();
		cam.setToOrtho(false, 800, 480 );

		//position of finger on screen
		pos = new Vector3(Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2, 0); //starts in middle of screen

		//gesture detector
		gd = new GestureDetector(this);
		Gdx.input.setInputProcessor(gd);

		//images
		img = new Texture("bucket.png");
		img2 = new Texture("droplet.png");
		img3 = new Texture ("bad droplet.png");

		//shape 1
		bucket = new Rectangle();
		bucket.x = 368;
		bucket.y = 0;
		bucket.width = 64;
		bucket.height = 64;

		//shape 2
		raindropArr = new Array<Raindrop>();
		spawnRaindrop();

		//score font
		font = new BitmapFont();
		font.getData().setScale(2);

		//sprite batch
		batch = new SpriteBatch();

	}

	@Override
	public void resize(int width, int height) {

	}

	@Override
	public void render () {
		//DRAWING
		//screen
		Gdx.gl.glClearColor(0, 0, 0.2f, 1); //blue background color
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); //clear screen

		//LOGIC
		cam.update(); //update cam every frame of game
		batch.setProjectionMatrix(cam.combined); //makes canvas of sprite batch same as camera

		//sprite drawing
		batch.begin(); //open buffer
		font.draw(batch, "score: " + score, 0, 400);
		batch.draw(img, bucket.x, bucket.y);
		for (int i = 0; i < raindropArr.size; i++) { //draw textures to rectangle objects
			if (raindropArr.get(i).good == true ) {
				batch.draw(img2, raindropArr.get(i).raindrop.x, raindropArr.get(i).raindrop.y);
			}else {
				batch.draw(img3, raindropArr.get(i).raindrop.x, raindropArr.get(i).raindrop.y);
			}
		}

		//every 1 second, creates a new Raindrop with random x coordinate
		if (TimeUtils.nanoTime() - lastRaindropTime > 1000000000 - (dropSpeed*25000000)) {
			spawnRaindrop();
		}

		System.out.println(raindropArr.get(0).raindrop.y); //prints y position of a raindrop

		batch.end(); //close buffer

		//update x position of bucket
		bucket.x = pos.x - (bucket.width/2);

		//prevent bucket from leaving screen
		if(bucket.x < 0) bucket.x = 0;
		if(bucket.x > 800 - bucket.width) bucket.x = 800 - bucket.width;

		//change y position of raindrops
		for (int i = 0; i < raindropArr.size; i++) {
			Raindrop raindrop = raindropArr.get(i);
			raindrop.raindrop.y = (int) (raindrop.raindrop.y - ((200 + (raindrop.speed*50))* Gdx.graphics.getDeltaTime()));
		}

		//if character is touched by another object
		for (int i = 0; i < raindropArr.size; i++) {
			Raindrop raindrop = raindropArr.get(i);
			if (raindrop.raindrop.overlaps(bucket)) {
				System.out.println("Raindrop is touching bucket.");
				if (raindrop.good == true){
					score++; //score increments by 1
				}else{ // score returns to previous multiple of 10
					if (score % 10 == 0){
						score -= 10;
					}else{
						score = score - (score % 10);
					}
				}
				raindropArr.removeIndex(i);
			}

			//if raindrop leaves screen
			if (raindrop.raindrop.y == 0) {
				System.out.println("Raindrop is offscreen.");
				if (raindrop.good == true) {
					score--; //score decrements by 1
				}
				raindropArr.removeIndex(i);
			}
		}

		//keeps track of score state
		scoreState();

	}

	//generates raindrops with random x coordinate at the top of the screen
	public void spawnRaindrop(){
		int goodOrBad = MathUtils.random(0,5);
		Raindrop raindrop = new Raindrop(fall); //creates new raindrop object in Raindrop class
		if (goodOrBad == 0){
			raindrop.good = false;
		}
		raindropArr.add(raindrop);
		lastRaindropTime = TimeUtils.nanoTime();
		return;
	}

	//increases speed of raindrops depending on current score
	public void scoreState(){
		if (score > 0 && score % 10 == 0){
			fall++; //increase speed of raindrops
			dropSpeed++; //increase spawn of raindrops
		}
		return;
	}

	//python test
	public String sayHello(){
		return "hello";
	}

	@Override
	public void pause() {

	}

	@Override
	public void resume() {

	}

	@Override
	public void dispose () {
		batch.dispose();
		img.dispose();
		img2.dispose();
		img3.dispose();
	}

	@Override
	public boolean touchDown(float x, float y, int pointer, int button) {
		Vector3 touch = new Vector3(x,y, 0);
		cam.unproject(touch);
		//if bucket is being touched
		if (bucket.contains(touch.x,touch.y)){
			canMove = true;
		}
		return false;
	}

	@Override
	public boolean tap(float x, float y, int count, int button) {
		return false;
	}

	@Override
	public boolean longPress(float x, float y) {
		return false;
	}

	@Override
	public boolean fling(float velocityX, float velocityY, int button) {
		return false;
	}

	@Override
	public boolean pan(float x, float y, float deltaX, float deltaY) {
		if (canMove == true) {
			pos.set(x, y, 0); //change position
			cam.unproject(pos);
		}
		return false;
	}

	@Override
	public boolean panStop(float x, float y, int pointer, int button) {
		canMove = false;
		return false;
	}

	@Override
	public boolean zoom(float initialDistance, float distance) {
		return false;
	}

	@Override
	public boolean pinch(Vector2 initialPointer1, Vector2 initialPointer2, Vector2 pointer1, Vector2 pointer2) {
		return false;
	}

	@Override
	public void pinchStop() {

	}
}