package net.firstspring.javatter.listplugin;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.lang.reflect.Method;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class TimelineTable extends JTable
{
	private static final long serialVersionUID = 1L;

	Method m;

	public DefaultTableModel model = new DefaultTableModel(new Object[] { "" }, 0);

	public TimelineTable()
	{
		super();
		this.setTableHeader(null);
		this.setIntercellSpacing(new Dimension(0, 0));
		this.setDefaultEditor(Object.class, null);
		this.setModel(model);
		this.setDefaultRenderer(Object.class, new TableCellRenderer()
		{
			public Component getTableCellRendererComponent(JTable table, Object value, boolean select, boolean focus, int row, int column)
			{
				JPanel p = (JPanel) value;
				p.setMinimumSize(null);
				p.setPreferredSize(null);
				p.setMaximumSize(null);
				p.setSize(p.getPreferredSize()); // processMouseEventのために必要
				int height = p.getPreferredSize().height;
				// 呼び出しがループするのを防ぐ
				if (table.getRowHeight(row) != height)
				{
					table.setRowHeight(row, p.getPreferredSize().height);
				}
				return p;
			}
		});
		try
		{
			m = Component.class.getDeclaredMethod("processMouseEvent", MouseEvent.class);
			m.setAccessible(true);
		}
		catch (Exception e)
		{
		}
	}

	public void addTop(Object o)
	{
		model.insertRow(0, new Object[] { o });
	}

	public void addLast(Object o)
	{
		model.addRow(new Object[] { o });
	}

	@Override
	public void processMouseEvent(MouseEvent e)
	{
		Component c = null;
		try
		{
			int row = rowAtPoint(new Point(e.getX(), e.getY()));
			c = prepareRenderer(this.getCellRenderer(row, 0), row, 0);
			int height = 0;
			for (int i = 0; i < row; i++)
			{
				height += getRowHeight(i);
			}
			// PopupMenu等のためにaddして終わったらremoveしてやる（親コンポーネントを要求するので）
			this.add(c);
			c.setLocation(0, height);
			MouseEvent me = new MouseEvent(c, e.getID(), e.getWhen(), e.getModifiers(), e.getX(), e.getY() - height, e.getClickCount(), e.isPopupTrigger());
			Component cc = SwingUtilities.getDeepestComponentAt(c, e.getX(), e.getY() - height);
			m.invoke(cc, SwingUtilities.convertMouseEvent(c, me, cc));
		}
		catch (Throwable t)
		{
		}
		finally
		{
			this.remove(c);
			this.repaint();
		}

	}

}
