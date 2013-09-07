package net.firstspring.javatter.listplugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

import com.orekyuu.javatter.account.TwitterManager;
import com.orekyuu.javatter.plugin.JavatterPlugin;
import com.orekyuu.javatter.plugin.JavatterPluginLoader;
import com.orekyuu.javatter.view.IJavatterTab;

public class ListPlugin extends JavatterPlugin{
	
	protected static ListPlugin instance;
	protected static JTabbedPane listTab;
	JPopupMenu menu;

	@Override
	public void init() {
		instance = this;
		menu = new JPopupMenu();
		JMenuItem item = new JMenuItem("閉じる");
		item.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				ListTab tab = ((ListTabScrollPane)listTab.getSelectedComponent()).tab;
				tab.refreshTask.cancel();
				listTab.remove(tab.tp);
			}
		});
		menu.add(item);
	}
	
	public ListTab createTab()
	{
		ListTab tab = new ListTab(this.getMainView(), JavatterPluginLoader.getTweetObjectBuilder());
		this.addUserStreamTab("リスト", tab);
		if(listTab == null)
		{
			listTab = (JTabbedPane)tab.tp.getParent();
			listTab.addMouseListener(new MouseAdapter()
			{
				
				@Override
				public void mouseClicked(MouseEvent e)
				{
					if(SwingUtilities.isRightMouseButton(e))
					{
						if(listTab.getSelectedComponent() instanceof ListTabScrollPane)
						{
							e.getX();
							e.getY();
							listTab.getX();
							menu.getX();
							menu.show(listTab, e.getX(), e.getY());
						}						

					}
				}
				
			});
		}
		return tab;
	}

	@Override
	protected IJavatterTab getPluginConfigViewObserver()
	{
		return new ListConfigView(getSaveData());
	}

	@Override
	public String getPluginName()
	{
		return "ListPlugin";
	}

	@Override
	public String getVersion()
	{
		return "#2";
	}
	
	

}
