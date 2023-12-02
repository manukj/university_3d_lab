package model;

import utils.*;
import gmaths.*;
import java.nio.*;
import com.jogamp.common.nio.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.util.*;
import com.jogamp.opengl.util.awt.*;
import com.jogamp.opengl.util.glsl.*;
import com.jogamp.opengl.util.texture.*;
import com.jogamp.opengl.util.texture.awt.*;
import com.jogamp.opengl.util.texture.spi.JPEGImage;

import constants.Constants;

/**
 * I declare that this code is my own work
 * Author: Manu Kenchappa Junjanna
 */
public class AlienModel {
    private Camera camera;
    private Light[] lightIn;
    private SGNode root;
    private ModelMultipleLights sphere;
    private TransformNode rockAnimation, headRollAnimation;
    private double startTime;
    private boolean isRocking = true;
    private boolean isRolling = true;
    private float rockSpeed = 2f;
    private float rollSpeed = 2f;
    private boolean isRollingFrontNBack = true;

    public AlienModel(GL3 gl, Camera camera, Light[] light, float xPosition, double startTime,
            boolean isRollingFrontNBack) {
        this.startTime = startTime;
        this.camera = camera;
        this.lightIn = light;
        this.isRollingFrontNBack = isRollingFrontNBack;
        // This change is just to show different speed for different aliens for the
        // first time
        if (isRollingFrontNBack) {
            rollSpeed = 1f;
            rockSpeed = 2f;
        } else {
            rollSpeed = 2f;
            rockSpeed = 1f;
        }
        sphere = makeSphere(gl);

        float bodyScale = 2f;
        float headScale = 1f;
        float eyeWidth = 0.25f;
        float eyeDepth = 0.15f;
        float eyeScale = 0.2f;

        float earLength = 0.8f;
        float earWidth = 0.1f;
        float earDepth = 0.3f;

        float antennaLength = 0.4f;
        float anteenaScale = 0.06f;
        float antennaSphereScale = 0.2f;

        float armLength = 1f;
        float armScale = 0.2f;

        // Scene graph nodes
        root = new NameNode("root");
        TransformNode rootTranslate = new TransformNode("root translate", Mat4Transform.translate(xPosition, 0, 0));
        rockAnimation = new TransformNode("rock animation", Mat4Transform.rotateAroundZ(0));

        // body and its transform
        NameNode body = new NameNode("body");
        Mat4 m = Mat4Transform.scale(bodyScale, bodyScale, bodyScale);
        m = Mat4.multiply(m, Mat4Transform.translate(0, 0.5f, 0));
        TransformNode bodyTransform = new TransformNode("body transform", m);
        ModelNode bodyShape = new ModelNode("Sphere(body)", sphere);

        // head and its transform
        NameNode head = new NameNode("head");
        TransformNode headTranslate = new TransformNode("head translate",
                Mat4Transform.translate(0, bodyScale + headScale * 0.5f, 0));
        headRollAnimation = new TransformNode("head roll animation", Mat4Transform.rotateAroundZ(0));
        m = Mat4Transform.scale(headScale, headScale, headScale);
        TransformNode headTransform = new TransformNode("head transform", m);
        ModelNode headShape = new ModelNode("Sphere(head)", sphere);

        // antenna and its transform
        NameNode antenna = new NameNode("antenna");
        TransformNode antennaTranslate = new TransformNode("antenna translate",
                Mat4Transform.translate(0, headScale * 0.5f + antennaLength * 0.5f, 0));
        m = Mat4Transform.scale(anteenaScale, antennaLength, anteenaScale);
        TransformNode antennaTransform = new TransformNode("antenna transform", m);
        ModelNode antennaShape = new ModelNode("Sphere(antenna)", sphere);

        NameNode antennaSphere = new NameNode("antenna sphere");
        m = Mat4Transform.translate(0, antennaLength * 0.5f + antennaSphereScale * 0.5f, 0);
        m = Mat4.multiply(m, Mat4Transform.scale(antennaSphereScale, antennaSphereScale, antennaSphereScale));
        TransformNode antennaSphereTransform = new TransformNode("antenna spheretransform", m);
        ModelNode antennaSphereShape = new ModelNode("Sphere(antenna sphere)", sphere);

        // eyes and its transform
        NameNode leftEye = new NameNode("left eye");
        m = Mat4Transform.translate(headScale * 0.18f, headScale * 0.20f, headScale * 0.38f);
        m = Mat4.multiply(m, Mat4Transform.scale(eyeWidth, eyeScale, eyeDepth));
        TransformNode leftEyeTransform = new TransformNode("left eye transform", m);
        ModelNode leftEyeShape = new ModelNode("Sphere(left eye)", sphere);

        NameNode rightEye = new NameNode("right eye");
        m = Mat4Transform.translate(-headScale * 0.18f, headScale * 0.20f, headScale * 0.38f);
        m = Mat4.multiply(m, Mat4Transform.scale(eyeWidth, eyeScale, eyeDepth));
        TransformNode rightEyeTransform = new TransformNode("right eye transform", m);
        ModelNode rightEyeShape = new ModelNode("Sphere(right eye)", sphere);

        // ears and its transform
        NameNode leftEar = new NameNode("left ear");
        m = Mat4Transform.translate(headScale * 0.5f, earLength * 0.5f, 0);
        m = Mat4.multiply(m, Mat4Transform.scale(earWidth, earLength, earDepth));
        TransformNode leftEarTransform = new TransformNode("left ear transform", m);
        ModelNode leftEarShape = new ModelNode("Sphere(left ear)", sphere);

        NameNode rightEar = new NameNode("right ear");
        m = Mat4Transform.translate(-headScale * 0.5f, earLength * 0.5f, 0);
        m = Mat4.multiply(m, Mat4Transform.scale(earWidth, earLength, earDepth));
        TransformNode rightEarTransform = new TransformNode("right ear transform", m);
        ModelNode rightEarShape = new ModelNode("Sphere(right ear)", sphere);

        // arms and its transform
        NameNode leftArm = new NameNode("left arm");
        m = Mat4Transform.translate((bodyScale + armScale) * 0.5f, (bodyScale + armLength) * 0.5f, 0);
        m = Mat4.multiply(m, Mat4Transform.rotateAroundZ(-20));
        m = Mat4.multiply(m, Mat4Transform.scale(armScale, armLength, armScale));
        TransformNode leftArmTransform = new TransformNode("left arm transform", m);
        ModelNode leftArmShape = new ModelNode("Sphere(left arm)", sphere);

        NameNode rightArm = new NameNode("right arm");
        m = Mat4Transform.translate(-(bodyScale + armScale) * 0.5f, (bodyScale + armLength) * 0.5f, 0);
        m = Mat4.multiply(m, Mat4Transform.rotateAroundZ(20));
        m = Mat4.multiply(m, Mat4Transform.scale(armScale, armLength, armScale));
        TransformNode rightArmTransform = new TransformNode("right arm transform", m);
        ModelNode rightArmShape = new ModelNode("Sphere(right arm)", sphere);

        // body -> root
        root.addChild(rootTranslate);
        rootTranslate.addChild(rockAnimation);
        rockAnimation.addChild(body);
        body.addChild(bodyTransform);
        bodyTransform.addChild(bodyShape);

        // head -> body
        body.addChild(headTranslate);
        headTranslate.addChild(headRollAnimation);
        headRollAnimation.addChild(head);
        head.addChild(headTransform);
        headTransform.addChild(headShape);

        // antenna -> head
        head.addChild(antennaTranslate);
        antennaTranslate.addChild(antenna);
        antenna.addChild(antennaTransform);
        antennaTransform.addChild(antennaShape);
        // antennaSphere -> antenna
        antenna.addChild(antennaSphere);
        antennaSphere.addChild(antennaSphereTransform);
        antennaSphereTransform.addChild(antennaSphereShape);

        // leftEye -> head
        head.addChild(leftEye);
        leftEye.addChild(leftEyeTransform);
        leftEyeTransform.addChild(leftEyeShape);
        // rightEye -> head
        head.addChild(rightEye);
        rightEye.addChild(rightEyeTransform);
        rightEyeTransform.addChild(rightEyeShape);

        // leftEar -> head
        head.addChild(leftEar);
        leftEar.addChild(leftEarTransform);
        leftEarTransform.addChild(leftEarShape);

        // rightEar -> head
        head.addChild(rightEar);
        rightEar.addChild(rightEarTransform);
        rightEarTransform.addChild(rightEarShape);

        // leftArm -> body
        body.addChild(leftArm);
        leftArm.addChild(leftArmTransform);
        leftArmTransform.addChild(leftArmShape);

        // rightArm -> body
        body.addChild(rightArm);
        rightArm.addChild(rightArmTransform);
        rightArmTransform.addChild(rightArmShape);

        root.update();
    }

    public void render(GL3 gl) {
        // Controling the animation
        if (isRocking) {
            startRockAnimation(rockSpeed);
        } else {
            stopRockAnimation();
        }
        if (isRolling) {
            if (isRollingFrontNBack) {
                startHeadRollFrontNBackAnimation(rollSpeed);
            } else {
                startHeadRollSideToSideAnimation(rollSpeed);
            }
        } else {
            stopHeadRollAnimation();
        }
        root.draw(gl);
    }

    public void dispose(GL3 gl) {
        sphere.dispose(gl);
    }

    private ModelMultipleLights makeSphere(GL3 gl) {
        String name = "sphere";
        Mesh mesh = new Mesh(gl, Sphere.vertices.clone(), Sphere.indices.clone());
        Shader shader = new Shader(gl, Constants.VERTEX_SHADER_STANDARD_PATH,
                Constants.FRAGMENT_SHADER_MULTIPLE_LIGHTS_1_TEXTURE);
        Material material = new Material(new Vec3(1.0f, 0.5f, 0.31f), new Vec3(1.0f, 0.5f, 0.31f),
                new Vec3(0.5f, 0.5f, 0.5f), 32.0f);
        Mat4 modelMatrix = Mat4.multiply(Mat4Transform.scale(4, 4, 4), Mat4Transform.translate(0, 0.5f, 0));
        ModelMultipleLights sphere = new ModelMultipleLights(name, mesh, modelMatrix, shader, material, lightIn,
                camera);
        return sphere;
    }

    /**
     * Starts the rock animation for the alien model.
     * 
     * @param rollSpeed the speed at which the model rolls
     */
    private void startRockAnimation(float rollSpeed) {
        double elapsedTime = AssignmentUtil.getSeconds() - startTime;
        float rollAngleMax = 30;
        float rollAngleDelta = rollSpeed * (float) elapsedTime;
        float rollAngle = rollAngleMax * (float) Math.sin(rollAngleDelta);
        rockAnimation.setTransform(Mat4Transform.rotateAroundZ(rollAngle));
        rockAnimation.update();
    }

    /**
     * Stops the rock animation by resetting the transform of the rockAnimation
     * object.
     */
    public void stopRockAnimation() {
        rockAnimation.setTransform(Mat4Transform.rotateAroundZ(0));
        rockAnimation.update();
    }

    /**
     * Starts the head roll animation for the alien in Front and Back moment.
     * 
     * @param rollSpeed
     */
    private void startHeadRollFrontNBackAnimation(float rollSpeed) {
        double elapsedTime = AssignmentUtil.getSeconds() - startTime;
        float rollAngleMax = 20;
        float rollAngleDelta = rollSpeed * (float) elapsedTime;
        float rollAngle = rollAngleMax * (float) Math.sin(rollAngleDelta);
        headRollAnimation.setTransform(Mat4Transform.rotateAroundX(rollAngle));
        headRollAnimation.update();
    }

    /**
     * Starts the head roll animation for the alien in Side to Side moment.
     * 
     * @param rollSpeed
     */
    private void startHeadRollSideToSideAnimation(float rollSpeed) {
        double elapsedTime = AssignmentUtil.getSeconds() - startTime;
        float rollAngleMax = 20;
        float rollAngleDelta = rollSpeed * (float) elapsedTime;
        float rollAngle = rollAngleMax * (float) Math.sin(rollAngleDelta);
        headRollAnimation.setTransform(Mat4Transform.rotateAroundY(rollAngle));
        headRollAnimation.update();
    }

    /**
     * Stops the head roll animation by resetting the transform of the
     * headRollAnimation object.
     */
    private void stopHeadRollAnimation() {
        headRollAnimation.setTransform(Mat4Transform.rotateAroundX(0));
        headRollAnimation.update();
        headRollAnimation.setTransform(Mat4Transform.rotateAroundY(0));
        headRollAnimation.update();
    }

    /**
     * Toggles states of rock animation from on to off and vice versa.
     */
    public void toggleRollAnimation() {
        isRolling = !isRolling;
    }

    /**
     * Toggles states of roll animation from on to off and vice versa.
     */
    public void toggleRockAnimation() {
        isRocking = !isRocking;
    }

    /**
     * Changes the direction of the roll animation from front and back to side to
     * side and vice versa.
     */
    public void changeRollDirection() {
        isRollingFrontNBack = !isRollingFrontNBack;
    }

    public void increaseRockSpeed() {
        rockSpeed += 0.5f;
    }

    public void decreaseRockSpeed() {
        rockSpeed -= 0.5f;
    }

    public void increaseRollSpeed() {
        rollSpeed += 0.5f;
    }

    public void decreaseRollSpeed() {
        rollSpeed -= 0.5f;
    }
}