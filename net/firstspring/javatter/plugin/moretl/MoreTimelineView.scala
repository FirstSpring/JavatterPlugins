package net.firstspring.javatter.plugin.moretl

import com.orekyuu.javatter.account.TwitterManager
import com.orekyuu.javatter.main.Main
import com.orekyuu.javatter.plugin.JavatterPluginLoader
import com.orekyuu.javatter.util.TweetObjectFactory
import javax.swing._
import javax.swing.border.LineBorder
import java.awt._
import java.awt.event._
import twitter4j.Paging

class MoreTimelineView extends ActionListener with Runnable
{
  val tl = new JPanel
  val button = new JButton
  val paging = new Paging(1, 200)
  var t = new Thread(this)
  var first = true
  
  def get: Component = 
  {
    val panel = new JPanel(new BorderLayout)
    tl.setLayout(new BoxLayout(tl, 3))
    val jsp = new JScrollPane(tl, 22, 31)
    jsp.getVerticalScrollBar.setUnitIncrement(20)
    panel.add(jsp, BorderLayout.CENTER)
    button.addActionListener(this)
    panel.add(button, BorderLayout.PAGE_END)
    t.start
    panel
  }
  
  override def actionPerformed(e: ActionEvent)
  {
    if(t == null)
    {
      t = new Thread(this)
      t.start
    }
  }
  
  override def run = 
  {
    button.setText("Loading...")
    val home = TwitterManager.getInstance.getTwitter.getHomeTimeline(paging)
    val i = home.iterator
    if(paging.getMaxId != -1)
    {
      i.next
      System.out.println("feel")
    }
    while(i.hasNext)
    {
      tl.add(new TweetObjectFactory(i.next, JavatterPluginLoader.getTweetObjectBuilder).createTweetObject(Main.getMainView).getComponent)
    }
    paging.maxId(home.get(home.size - 1).getId)
    button.setText("Load")
    t = null
  }
}