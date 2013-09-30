package net.firstspring.javatter.plugin.moretl

import com.orekyuu.javatter.plugin.{JavatterPlugin, JavatterPluginLoader}
import com.orekyuu.javatter.view.IJavatterTab
import javax.swing.JPanel
import java.awt.Component

class MoreTimeline extends JavatterPlugin
{
  override def init = {}
  
  override def getPluginName = "MoreTimeline"
    
  override def getVersion = "#1"
    
  override def getPluginConfigViewObserver = new IJavatterTab
  {
    override def getComponent: Component = new MoreTimelineView().get
  }
}