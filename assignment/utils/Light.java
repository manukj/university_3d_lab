package utils;

import gmaths.*;
import java.nio.*;
import com.jogamp.common.nio.*;
import com.jogamp.opengl.*;

import constants.Constants;

public class Light {

  private Material material;
  private Vec3 position;
  private Mat4 model;
  private Shader shader;
  private Camera camera;
  public boolean isOn = true;
  private Vec4 lightColor = new Vec4(1.0f, 1.0f, 1.0f, 1.0f);
  public boolean isSpotLight = false;

  public Light(GL3 gl, int lightIndex) {
    // change : moved materials to constants and added support for spot light
    isSpotLight = lightIndex == 2;
    if (isSpotLight) {
      setMaterial(Constants.SPOT_LIGHT_MATERIAL);
    } else {
      setMaterial(Constants.MAIN_LIGHT_MATERIAL);
    }
    position = new Vec3(3f, 2f, 1f);
    model = new Mat4(1);
    shader = new Shader(gl, Constants.VERTEX_SHADER_LIGHT_PATH, Constants.FRAGMENT_SHADER_LIGHT_PATH);
    fillBuffers(gl);
  }

  public Light(GL3 gl, Material material) {
    this.material = material;
    position = new Vec3(3f, 2f, 1f);
    model = new Mat4(1);
    shader = new Shader(gl, Constants.VERTEX_SHADER_LIGHT_PATH, Constants.FRAGMENT_SHADER_LIGHT_PATH);
    fillBuffers(gl);
  }

  public void setPosition(Vec3 v) {
    position.x = v.x;
    position.y = v.y;
    position.z = v.z;
  }

  public void setPosition(float x, float y, float z) {
    position.x = x;
    position.y = y;
    position.z = z;
  }

  public Vec3 getPosition() {
    return position;
  }

  public void setMaterial(Material m) {
    material = m;
  }

  public void setColor(Vec4 color) {
    lightColor = color;
  }

  public Material getMaterial() {
    return material;
  }

  public void setCamera(Camera camera) {
    this.camera = camera;
  }

  public void render(GL3 gl) {
    render(gl, null);
  }

  public void render(GL3 gl, Mat4 model) {
    if (model == null) {
      model = new Mat4(1);
      model = Mat4.multiply(Mat4Transform.scale(0.3f, 0.3f, 0.3f), model);
      model = Mat4.multiply(Mat4Transform.translate(position), model);
    }

    Mat4 mvpMatrix = Mat4.multiply(camera.getPerspectiveMatrix(), Mat4.multiply(camera.getViewMatrix(), model));

    shader.use(gl);
    shader.setFloatArray(gl, "mvpMatrix", mvpMatrix.toFloatArrayForGLSL());
    // changes : added support for setting light color dynamically
    shader.setFloat(gl, "lightColor", lightColor.x, lightColor.y, lightColor.z, lightColor.w);

    gl.glBindVertexArray(vertexArrayId[0]);
    gl.glDrawElements(GL.GL_TRIANGLES, indices.length, GL.GL_UNSIGNED_INT, 0);
    gl.glBindVertexArray(0);
  }

  public void dispose(GL3 gl) {
    gl.glDeleteBuffers(1, vertexBufferId, 0);
    gl.glDeleteVertexArrays(1, vertexArrayId, 0);
    gl.glDeleteBuffers(1, elementBufferId, 0);
  }

  // ***************************************************
  /*
   * THE DATA
   */
  // anticlockwise/counterclockwise ordering

  private float[] vertices = new float[] { // x,y,z
      -0.5f, -0.5f, -0.5f, // 0
      -0.5f, -0.5f, 0.5f, // 1
      -0.5f, 0.5f, -0.5f, // 2
      -0.5f, 0.5f, 0.5f, // 3
      0.5f, -0.5f, -0.5f, // 4
      0.5f, -0.5f, 0.5f, // 5
      0.5f, 0.5f, -0.5f, // 6
      0.5f, 0.5f, 0.5f // 7
  };

  private int[] indices = new int[] {
      0, 1, 3, // x -ve
      3, 2, 0, // x -ve
      4, 6, 7, // x +ve
      7, 5, 4, // x +ve
      1, 5, 7, // z +ve
      7, 3, 1, // z +ve
      6, 4, 0, // z -ve
      0, 2, 6, // z -ve
      0, 4, 5, // y -ve
      5, 1, 0, // y -ve
      2, 3, 7, // y +ve
      7, 6, 2 // y +ve
  };

  private int vertexStride = 3;
  private int vertexXYZFloats = 3;

  // ***************************************************
  /*
   * THE LIGHT BUFFERS
   */

  private int[] vertexBufferId = new int[1];
  private int[] vertexArrayId = new int[1];
  private int[] elementBufferId = new int[1];

  private void fillBuffers(GL3 gl) {
    gl.glGenVertexArrays(1, vertexArrayId, 0);
    gl.glBindVertexArray(vertexArrayId[0]);
    gl.glGenBuffers(1, vertexBufferId, 0);
    gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBufferId[0]);
    FloatBuffer fb = Buffers.newDirectFloatBuffer(vertices);

    gl.glBufferData(GL.GL_ARRAY_BUFFER, Float.BYTES * vertices.length, fb, GL.GL_STATIC_DRAW);

    int stride = vertexStride;
    int numXYZFloats = vertexXYZFloats;
    int offset = 0;
    gl.glVertexAttribPointer(0, numXYZFloats, GL.GL_FLOAT, false, stride * Float.BYTES, offset);
    gl.glEnableVertexAttribArray(0);

    gl.glGenBuffers(1, elementBufferId, 0);
    IntBuffer ib = Buffers.newDirectIntBuffer(indices);
    gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, elementBufferId[0]);
    gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, Integer.BYTES * indices.length, ib, GL.GL_STATIC_DRAW);
    // gl.glBindVertexArray(0);
  }

  // change : added support for turning on and off the light
  public void turnOf() {
    setMaterial(Constants.LIGHT_OFF_MATERIAL);
    lightColor = Constants.LIGHT_OFF_COLOR;
    isOn = false;
  }

  public void turnOn() {
    setMaterial(Constants.MAIN_LIGHT_MATERIAL);
    lightColor = Constants.LIGHT_ON_COLOR;
    isOn = true;
  }

  public void turnOnSpotLight() {
    setMaterial(Constants.SPOT_LIGHT_MATERIAL);
    lightColor = Constants.SPOT_LIGHT_ON_COLOR;
    isOn = true;
  }

  // change : added support for dimming and brightening the light
  public void dim(int lightNumber) {
    if (isDimExceededLightThresold(lightNumber)) {
      turnOf();
    } else {
      material.setAmbient(material.getAmbient().x - 0.1f, material.getAmbient().y - 0.1f,
          material.getAmbient().z - 0.1f);
      material.setDiffuse(material.getDiffuse().x - 0.1f, material.getDiffuse().y - 0.1f,
          material.getDiffuse().z - 0.1f);
      material.setSpecular(material.getSpecular().x - 0.1f, material.getSpecular().y - 0.1f,
          material.getSpecular().z - 0.1f);
    }
  }

  private boolean isDimExceededLightThresold(int lightNumber) {
    if (lightNumber == 1) {
      // for main light, go until the specular x value is 0.5
      return material.getAmbient().x <= 0.0f &&
          material.getDiffuse().y <= 0.0f &&
          material.getSpecular().z <= 0.5;
    } else {
      // for spot light, the ambient starts from 0.0f, so we can't go beyond -0.5f
      return material.getAmbient().x <= 0.0f &&
          material.getDiffuse().y <= 0.0f &&
          material.getSpecular().z <= 0.5;
    }
  }

  public void brighten(int lightNumber) {
    if (isBrightenExceededLightThresold(lightNumber)) {
      if (lightNumber == 1) {
        turnOn();
      } else {
        turnOnSpotLight();
      }
    } else {
      material.setAmbient(material.getAmbient().x + 0.1f, material.getAmbient().y + 0.1f,
          material.getAmbient().z + 0.1f);
      material.setDiffuse(material.getDiffuse().x + 0.1f, material.getDiffuse().y + 0.1f,
          material.getDiffuse().z + 0.1f);
      material.setSpecular(material.getSpecular().x + 0.1f, material.getSpecular().y + 0.1f,
          material.getSpecular().z + 0.1f);

    }
  }

  public boolean isBrightenExceededLightThresold(int lightNumber) {
    if (lightNumber == 1) {
      // for main light, the ambient starts from 0.3f, so we can't go beyond 0.7f
      return material.getAmbient().x >= 0.7f;
    } else {
      // for spot light, the ambient starts from 0.0f, so we can't go beyond 0.5f
      return material.getAmbient().x >= 0.0f &&
          material.getDiffuse().y >= 0.5f &&
          material.getSpecular().z >= 1;
    }
  }

  public boolean isSpotLightOn() {
    return getMaterial().getAmbient().magnitude() == 0.0f &&
        getMaterial().getDiffuse().magnitude() == 0.5f &&
        getMaterial().getSpecular().magnitude() == 1.0f;
  }
}