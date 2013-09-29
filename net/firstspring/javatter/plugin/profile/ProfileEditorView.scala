package net.firstspring.javatter.plugin.profile

import com.orekyuu.javatter.account.TwitterManager
import com.orekyuu.javatter.view.IJavatterTab
import javax.swing._
import javax.swing.border.LineBorder
import java.awt._
import java.awt.event._

class ProfileEditorView extends IJavatterTab with ActionListener
{
  val border = new LineBorder(new Color(128, 128, 128))
  
  val name = new JTextArea(1, 34)
  name.setBorder(border)
  val url = new JTextArea(1, 34)
  url.setBorder(border)
  val location = new JTextArea(1, 34)
  location.setBorder(border)
  val description = new JTextArea(4, 34)
  description.setBorder(border)
  
  var panel: JPanel = _
  
  override def getComponent: Component = 
  {
    val t = TwitterManager.getInstance.getTwitter
    try
    {
      val user = t.showUser(t.getId)
      name.setText(user.getName)
      url.setText(user.getURL)
      location.setText(user.getLocation)
      description.setText(user.getDescription)
    }
    catch
    {
      case _ =>
        {
          name.setText("")
          url.setText("")
          location.setText("")
          description.setText("")
        }
    }
    val tab = new JTabbedPane
    panel = new JPanel
    panel.setLayout(null)
    val util = new LayoutUtil(5,5)
    var label = new JLabel("名前")
    util.layout(label, 1)
    panel.add(label)
    util.layout(name, 1)
    panel.add(name)
    label = new JLabel("所在地")
    util.layout(label, 1)
    panel.add(label)
    util.layout(location, 1)
    panel.add(location)
    label = new JLabel("ホームページ")
    util.layout(label, 1)
    panel.add(label)
    util.layout(url, 1)
    panel.add(url)
    label = new JLabel("自己紹介")
    util.layout(label, 1)
    panel.add(label)
    util.layout(description, 1)
    panel.add(description)
    var button = new JButton("プロフィール変更")
    button.addActionListener(this)
    button.setActionCommand("profile")
    util.layout(button, 1)
    panel.add(button)
    button = new JButton("アイコン変更")
    button.addActionListener(this)
    button.setActionCommand("icon")
    util.layout(button, 0)
    panel.add(button)
    panel
  }
  
  override def actionPerformed(e: ActionEvent) = 
  {
    val t = TwitterManager.getInstance.getTwitter
    e.getActionCommand match
    {
      case "profile" => 
        {
          t.updateProfile(name.getText, url.getText, location.getText, description.getText)
          JOptionPane.showMessageDialog(panel, "プロフィールを変更しました")
        }
      case "icon" =>
        {
          val choose = new JFileChooser(".")
          if(choose.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION)
          {
            t.updateProfileImage(choose.getSelectedFile)
            JOptionPane.showMessageDialog(panel, "アイコンを変更しました")
          }
        }
      case _ =>
    }
    
  }
}