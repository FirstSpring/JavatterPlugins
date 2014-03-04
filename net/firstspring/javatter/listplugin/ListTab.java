package net.firstspring.javatter.listplugin;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import twitter4j.Paging;
import twitter4j.Status;

import com.orekyuu.javatter.account.TwitterManager;
import com.orekyuu.javatter.plugin.TweetObjectBuilder;
import com.orekyuu.javatter.util.BackGroundColor;
import com.orekyuu.javatter.util.TweetObjectFactory;
import com.orekyuu.javatter.view.IJavatterTab;
import com.orekyuu.javatter.viewobserver.UserEventViewObserver;

public class ListTab implements IJavatterTab, AdjustmentListener, ActionListener
{

	UserEventViewObserver observer;
	List<TweetObjectBuilder> builders;

	TimelineTable table;
	ListTabScrollPane tp;

	volatile Queue<JPanel> queue = new ConcurrentLinkedQueue<JPanel>();
	boolean queueFlag;
	boolean queueEvent;

	public static final Timer refresher = new Timer();
	public RefreshTask refreshTask = new RefreshTask();
	public int listId;
	public int amount;
	public String listName = "Loading";
	public Status top;
	public Status last;

	public JButton load;

	public ListTab(UserEventViewObserver observer, List<TweetObjectBuilder> builders)
	{
		this.observer = observer;
		this.builders = builders;
		table = new TimelineTable();
		load = new JButton("more load");
		load.addActionListener(this);
		table.addLast(load);
		table.setBackground(BackGroundColor.color);
		tp = new ListTabScrollPane(this);
		tp.setViewportView(this.table);
		tp.getVerticalScrollBar().setUnitIncrement(20);
		tp.getVerticalScrollBar().addAdjustmentListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		load.setEnabled(false);
		load.setText("loading...");
		new Thread()
		{
			public void run()
			{
				try
				{
					List<Status> l = TwitterManager.getInstance().getTwitter().getUserListStatuses(listId, new Paging(1, amount, 1, last.getId()));
					l.remove(0);
					if(!l.isEmpty())
					{
						last = l.get(l.size() - 1);
					}
					final JPanel[] p = new JPanel[l.size()];
					for(int i = 0; i < l.size(); i++)
					{
						p[i] = createObject(l.get(i));
					}
					SwingUtilities.invokeLater(new Runnable()
					{
						public void run()
						{
							table.model.removeRow(table.model.getRowCount() - 1);
							for(JPanel panel : p)
							{
								table.addLast(panel);
							}
							load.setEnabled(true);
							load.setText("more load");
							table.addLast(load);
						}
					});
				} catch(Exception ex)
				{
					load.setEnabled(true);
					load.setText("more load");
				}
			}
		}.start();
	}

	class RefreshTask extends TimerTask
	{

		@Override
		public void run()
		{
			try
			{
				List<Status> l = TwitterManager.getInstance().getTwitter().getUserListStatuses(listId, new Paging(1, 200, top.getId()));
				top = l.get(0);
				Collections.reverse(l);
				for(Status s : l)
				{
					addStatus(s);
				}
			} catch(Exception e)
			{

			}
		}
	}

	public void addStatus(Status stat)
	{
		final JPanel p = createObject(stat);
		if(tp.getVerticalScrollBar().getValue() == 0 && !queueFlag)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					table.addTop(p);
				}
			});
		} else
		{
			queue.add(p);
			setNumber(queue.size());
		}
	}

	public synchronized void setNumber(int num)
	{
		JTabbedPane tab = (JTabbedPane)tp.getParent();
		for(int i = 0; i < tab.getTabCount(); i++)
		{
			if(tab.getComponentAt(i) == this.tp)
			{
				if(num != 0)
				{
					tab.setTitleAt(i, listName + "(" + num + ")");
				} else
				{
					tab.setTitleAt(i, listName);
				}
			}
		}
	}

	@Override
	public Component getComponent()
	{
		return tp;
	}

	public JPanel createObject(Status status)
	{
		TweetObjectFactory factory = new TweetObjectFactory(status, builders);
		return (JPanel)factory.createTweetObject(this.observer).getComponent();
	}

	@Override
	public void adjustmentValueChanged(AdjustmentEvent arg0)
	{
		if(arg0.getValue() == 0)
		{
			if(queueEvent)
			{
				return;
			}
			queueEvent = true;
			Thread th = new Thread()
			{
				@Override
				public void run()
				{
					queueFlag = true;
					final Queue<JPanel> q = new LinkedList<JPanel>();
					while(!queue.isEmpty())
					{
						q.add(queue.poll());
					}
					SwingUtilities.invokeLater(new Runnable()
					{
						public void run()
						{
							final int c = q.size();
							while(!q.isEmpty())
							{
								table.addTop(q.poll());
							}
							for(int i = 0; i < c; i++)
							{
								table.prepareRenderer(ListTab.this.table.getCellRenderer(i, 0), i, 0);
							}
							SwingUtilities.invokeLater(new Runnable()
							{
								public void run()
								{
									tp.getVerticalScrollBar().setValue(table.getCellRect(c, 0, true).y);
								}
							});
							setNumber(0);
							queueFlag = false;
						}
					});
				}
			};
			th.start();
		} else
		{
			queueEvent = false;
		}
	}

}
