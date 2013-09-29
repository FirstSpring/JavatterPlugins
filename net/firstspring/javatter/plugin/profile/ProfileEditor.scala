package net.firstspring.javatter.plugin.profile

import java.io.File
import com.orekyuu.javatter.plugin.{JavatterPlugin, JavatterPluginLoader}
import com.orekyuu.javatter.view.IJavatterTab

class ProfileEditor extends JavatterPlugin
{
  override def init =
  {
    //JavatterPluginLoader.addLibrary(new File("./plugins/lib/scala-library.jar"), this.getClass.getClassLoader);
  }
  
  override def getPluginName = "Profile Editor"
    
  override def getVersion = "#1"
    
  override def getPluginConfigViewObserver: IJavatterTab = new ProfileEditorView

}