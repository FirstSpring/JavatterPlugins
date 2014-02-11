package net.firstspring.javatter.listplugin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

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
	JComboBox amount;
	JComboBox getCount;
	static JList<String> listsView = new JList();
	static List<UserList> lists;

	public ListConfigView(SaveData data)
	{
		this.data = data;

	}

	public void loadLists()
	{
		File data = new File(ListPlugin.instance.dir, "lists.dat");
		try(ObjectInputStream is = new ObjectInputStream(new FileInputStream(data)))
		{
			lists = (List<UserList>)is.readObject();
			String[] arr = new String[lists.size()];
			for(int i = 0; i < arr.length; i++)
			{
				arr[i] = lists.get(i).getName();
			}
			listsView.setListData(arr);
		} catch(Exception e)
		{
		}
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
		refreshInterval.addMouseWheelListener(new MouseWheelListener()
		{
			public void mouseWheelMoved(MouseWheelEvent e)
			{
				JSpinner s = (JSpinner)e.getComponent();
				Object value;
				if(e.getWheelRotation() < 0)
				{
					value = s.getNextValue();
				} else
				{
					value = s.getPreviousValue();
				}
				if(value != null)
				{
					s.setValue(value);
				}
			}
		});
		panel.add(refreshInterval);

		label = new JLabel("初期取得量");
		label.setSize(label.getPreferredSize());
		label.setLocation(5, 89);
		panel.add(label);

		amount = new JComboBox<Integer>(new Integer[] {20, 50, 100, 200});
		amount.setSize(50, amount.getPreferredSize().height);
		amount.setLocation(110, 85);
		panel.add(amount);

		label = new JLabel("×");
		label.setSize(label.getPreferredSize());
		label.setLocation(165, 89);
		panel.add(label);

		getCount = new JComboBox<Integer>(new Integer[] {1, 2, 3, 4, 5});
		getCount.setSize(50, getCount.getPreferredSize().height);
		getCount.setLocation(177, 85);
		panel.add(getCount);

		tab.add("リストタブ設定", panel);

		loadLists();

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
				File data = new File(ListPlugin.instance.dir, "lists.dat");
				try(ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(data)))
				{
					os.writeObject(t.getUserLists(t.getId()));
				} catch(Exception ex)
				{
				}
				loadLists();
			}
			if(src == selectButton)
			{
				Thread loader = new Thread(new LocalRunnable());
				loader.start();
			}
		} catch(Exception ex)
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
				ListTab tab = ListPlugin.instance.createTab();
				int listId = lists.get(listsView.getSelectedIndex()).getId();
				int am = (Integer)amount.getSelectedItem();
				List<Status> l = t.getUserListStatuses(listId, new Paging(1, am));
				for(int i = l.size() - 1; i >= 0; i--)
				{
					tab.addStatus(l.get(i));
				}
				tab.top = l.get(0);
				tab.last = l.get(l.size() - 1);
				tab.listId = listId;
				tab.amount = am;
				tab.listName = listName;
				tab.setNumber(tab.queue.size());
				int time = (Integer)refreshInterval.getValue();
				ListTab.refresher.schedule(tab.refreshTask, time * 1000, time * 1000);
			} catch(Exception e)
			{

			}
		}
	}

}
