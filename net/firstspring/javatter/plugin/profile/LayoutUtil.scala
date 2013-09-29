package net.firstspring.javatter.plugin.profile

import java.awt.Component

class LayoutUtil(xdef: Int, ydef: Int)
{
  var x = xdef
  var y = ydef
  var xx = 0
  var yy = 0
  
  def layout(c: Component, i: Int) = 
  {
    val pref = c.getPreferredSize
    c.setSize(pref)
    if(i == 0)
    {
      c.setLocation(x, y)
      x = x + pref.width + 5
      yy = Math.max(yy, pref.height + 5)
    }
    else
    {
      y += yy
      x = xdef
      c.setLocation(x, y)
      yy = pref.height + 5
      x = xdef + pref.width + 5
      
    }
  }
}