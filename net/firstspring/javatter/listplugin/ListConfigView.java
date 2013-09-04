package net.firstspring.javatter.listplugin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.UserList;

import com.orekyuu.javatter.account.TwitterManager;
import com.orekyuu.javatter.util.SaveData;
import com.orekyuu.javatter.view.IJavatterTab;

public class ListConfigView implements IJavatterTab, ActionListener
{
	
	SaveData data;
	JButton getButton;
	JButton selectButton;
	JSpinner refreshInterval;
	JComboBox getAmount;
	JComboBox getCount;
	static JList<String> listsView = new JList();
	static List<UserList> lists;
		
	public ListConfigView(SaveData data)
	{
		this.data=data;
	}

	@Override
	public Component getComponent()
	{
		JLabel label;
		JTabbedPane tab = new JTabbedPane();
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		getButton = new JButton("リスト一覧取得");
		getButton.addActionListener(this);
		panel.add(getButton, BorderLayout.NORTH);
		JScrollPane jsp = new JScrollPane(listsView);
		panel.add(jsp, BorderLayout.CENTER);
		selectButton = new JButton("リストタブ追加");
		selectButton.addActionListener(this);
		panel.add(selectButton, BorderLayout.SOUTH);
		tab.add("リスト一覧", panel);
		
		panel = new JPanel();
		panel.setLayout(null);
		label = new JLabel("更新間隔はリストタブを追加した時の値に固定されます");
		label.setSize(label.getPreferredSize());
		label.setLocation(5, 5);
		panel.add(label);
		
		label = new JLabel("複数リストタブを追加するときはAPI規制にご注意ください");
		label.setSize(label.getPreferredSize());
		label.setLocation(5, 30);
		panel.add(label);
		
		label = new JLabel("更新間隔(10秒～)");
		label.setSize(label.getPreferredSize());
		label.setLocation(5, 55);
		panel.add(label);
		
		refreshInterval = new JSpinner(new SpinnerNumberModel(10, 10, Integer.MAX_VALUE, 1));
		refreshInterval.setSize(50, refreshInterval.getPreferredSize().height);
		refreshInterval.setLocation(110, 55);
		panel.add(refreshInterval);
		
		label = new JLabel("初期取得量");
		label.setSize(label.getPreferredSize());
		label.setLocation(5, 89);
		panel.add(label);
		
		getAmount = new JComboBox<Integer>(new Integer[]{20, 50, 100, 200});
		getAmount.setSize(50, getAmount.getPreferredSize().height);
		getAmount.setLocation(110, 85);
		panel.add(getAmount);
		
		label = new JLabel("×");
		label.setSize(label.getPreferredSize());
		label.setLocation(165, 89);
		panel.add(label);
		
		getCount = new JComboBox<Integer>(new Integer[]{1, 2, 3, 4, 5});
		getCount.setSize(50, getCount.getPreferredSize().height);
		getCount.setLocation(177, 85);
		panel.add(getCount);
		
		tab.add("リストタブ設定", panel);
		return tab;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		try
		{
			Twitter t = TwitterManager.getInstance().getTwitter();
			Object src = e.getSource();
			if(src == getButton)
			{
				lists = t.getUserLists(t.getId());
				DefaultListModel model = new DefaultListModel();
				for(int i = 0; i < lists.size(); i++)
				{
					model.addElement(lists.get(i).getName());
				}
				listsView.setModel(model);
			}
			if(src == selectButton)
			{
				Thread loader = new Thread(new LocalRunnable());
				loader.start();
			}
		}
		catch(Exception ex)
		{

		}
	}
	
	class LocalRunnable implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				Twitter t = TwitterManager.getInstance().getTwitter();
				String listName = listsView.getSelectedValue();
				ListTab tab = ListPlugin.instance.createTab(listName);
				int listId = lists.get(listsView.getSelectedIndex()).getId();
				List<Status> l = new ArrayList();
				int amount = (Integer)getAmount.getSelectedItem();
				int count = (Integer)getCount.getSelectedItem();
				for(int i = 1; i <= count; i++)
				{
					l.addAll(t.getUserListStatuses(listId, new Paging(i, amount)));
				}
				tab.panel.removeAll();
				for(int i = l.size() - 1; i >= 0; i--)
				{
					tab.addStatus(l.get(i));
				}
				tab.lastStat = l.get(0);
				tab.listId = listId;
				int time = (Integer)refreshInterval.getValue();
				tab.refresher.schedule(tab.refreshTask, time * 1000, time * 1000);
			}
			catch(Exception e)
			{

			}
		}
	}

}
