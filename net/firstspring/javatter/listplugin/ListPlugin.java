package net.firstspring.javatter.listplugin;

import com.orekyuu.javatter.plugin.JavatterPlugin;
import com.orekyuu.javatter.plugin.JavatterPluginLoader;
import com.orekyuu.javatter.view.IJavatterTab;
import com.orekyuu.javatter.view.MainWindowView;

public class ListPlugin extends JavatterPlugin{
	
	protected static ListPlugin instance;

	@Override
	public void init() {
		instance = this;
	}
	
	public ListTab createTab(String name)
	{
		ListTab tab = new ListTab(this.getMainView(), JavatterPluginLoader.getTweetObjectBuilder());
		tab.listName = name;
		this.addUserStreamTab("リスト", tab);
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
		return "1.0";
	}
	
	

}
