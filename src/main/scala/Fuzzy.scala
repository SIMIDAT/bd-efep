/**
  * The MIT License
  *
  * Copyright 2016 Ángel Miguel García-Vico.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */

/**
  * Created by Ángel Miguel García-Vico (agvico@ujaen.es) on 15/12/16.
  */
class Fuzzy extends Serializable{

  private var x0,x1,x3: Float = 0
  private var y: Float = 0

  /**
    * Methods to get the value of x0
    * @return          Value of x0
    */
  def getX0 : Float ={
    x0
  }

  /**
    * Methods to get the value of x1
    * @return          Value of x1
    */
  def getX1 : Float = {
    x1
  }

  /**
    * Methods to get the value of x3
    * @return          Value of x3
    */
  def getX3 : Float = {
    x3
  }


  /**
    * Method to set the values of x0, x1 y x3
    * @param vx0       Value for x0
    * @param vx1       Value for x1
    * @param vx3       Value for x3
    * @param vy        Value for y
    */
  def setVal (vx0: Float, vx1: Float, vx3: Float, vy: Float) = {
    x0 = vx0
    x1 = vx1
    x3 = vx3
    y  = vy
  }


  /**
    * Returns the belonging degree
    * @param X             Value to obtain the belonging degree
    * @return              Belonging degree
    */
  def Fuzzy (X: Float):Float = {
    if (X<=x0 || X>=x3)  // If value of X is not into range x0..x3
      return 0          // then pert. degree = 0
    if (X<x1)
      return (X-x0)*(y/(x1-x0))
    if (X>x1)
      return (x3-X)*(y/(x3-x1))
    y
  }


  def FuzzyT (X: Float, tun_param: Double) : Float = {
    val atun_param = tun_param.toFloat

    val ax0 = x0 + (((x3-x0)/2)*atun_param)
    val ax1 = x1 + (((x3-x0)/2)*atun_param)
    val ax3 = x3 + (((x3-x0)/2)*atun_param)


    if (X<=ax0 || X>=ax3)  // If value of X is not into range x0..x3
      return 0          // then pert. degree = 0
    if (X<x1)
      return (X-ax0)*(y/(ax1-ax0))
    if (X>x1)
      return (ax3-X)*(y/(ax3-ax1))
    y
  }

}
