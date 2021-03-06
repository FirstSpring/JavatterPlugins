package net.firstspring.javatter.listplugin;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Set;

import javax.swing.CellRendererPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class TimelineTable extends JTable implements MouseMotionListener
{
	private static final long serialVersionUID = 1L;

	int editRow = -1;

	public DefaultTableModel model = new DefaultTableModel(new Object[]{""}, 0);

	public TimelineTable()
	{
		super();
		setTableHeader(null);
		setIntercellSpacing(new Dimension(0, 0));
		setDefaultEditor(Object.class, new TableCellEditor()
		{
			@Override
			public void addCellEditorListener(CellEditorListener arg0){}

			@Override
			public void cancelCellEditing(){}

			@Override
			public Object getCellEditorValue(){return null;}

			@Override
			public boolean isCellEditable(EventObject arg0){return true;}

			@Override
			public void removeCellEditorListener(CellEditorListener arg0){}

			@Override
			public boolean shouldSelectCell(EventObject arg0){return true;}

			@Override
			public boolean stopCellEditing(){return true;}

			@Override
			public Component getTableCellEditorComponent(JTable table, Object value, boolean select, int row, int column)
			{
				return getDefaultRenderer(Object.class).getTableCellRendererComponent(table, value, select, false, row, column);
			}
		});
		setModel(model);
		setDefaultRenderer(Object.class, new TableCellRenderer()
		{
			Set<Object> resized = new HashSet<Object>();
			int tableSize;
			
			public Component getTableCellRendererComponent(JTable table, Object value, boolean select, boolean focus, int row, int column)
			{
				if(tableSize != table.getWidth())
				{
					resized.clear();
					tableSize = table.getWidth();
				}
				Component c = (Component)value;
				c.setMinimumSize(null);
				c.setPreferredSize(null);
				c.setMaximumSize(null);
				if(!resized.contains(c))
				{
					SwingUtilities.updateComponentTreeUI(c);
				}
				int height = c.getPreferredSize().height;
				// 呼び出しがループするのを防ぐ
				if(table.getRowHeight(row) != height)
				{
					table.setRowHeight(row, c.getPreferredSize().height);
				} else
				{
					resized.add(c);
				}
				return c;
			}
		});
		addMouseMotionListener(this);
	}

	public void addTop(Object o)
	{
		if(isEditing())
		{
			editRow = -1;
			removeEditor();
		}
		model.insertRow(0, new Object[]{o});
	}

	public void addLast(Object o)
	{
		if(isEditing())
		{
			editRow = -1;
			removeEditor();
		}
		model.addRow(new Object[]{o});
	}

	@Override
	public void removeEditor()
	{
		for(Component c : getComponents())
		{
			if(!(c instanceof CellRendererPane))
			{
				remove(c);
			}
		}
		super.removeEditor();
	}

	@Override
	public void mouseDragged(MouseEvent e){}

	@Override
	public void mouseMoved(MouseEvent e)
	{
		int r = rowAtPoint(e.getPoint());
		if(r != editRow)
		{
			removeEditor();
			editRow = r;
			editCellAt(editRow, 0);
		}
	}
}
