import org.apache.commons.lang.mutable.Mutable;
import org.snmp4j.agent.MOAccess;
import org.snmp4j.agent.mo.*;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.SMIConstants;
import org.snmp4j.smi.Variable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;


public class MOTableBuilder  implements MOChangeListener, MOTableRowListener{

	private MOTableSubIndex[] subIndexes = new MOTableSubIndex[] { new MOTableSubIndex(
			SMIConstants.SYNTAX_INTEGER) };
	private MOTableIndex indexDef = new MOTableIndex(subIndexes, false);

	private final List<MOColumn> columns = new ArrayList<MOColumn>();
	private final List<Variable[]> tableRows = new ArrayList<Variable[]>();
	private int currentRow = 0;
	private int currentCol = 0;

	private OID tableRootOid;

	private int colTypeCnt = 0;

	
	/**
	 * Specified oid is the root oid of this table
	 */
	public MOTableBuilder(OID oid) {
		this.tableRootOid = oid;
	}

	/**
	 * Adds all column types {@link MOColumn} to this table.
	 * Important to understand that you must add all types here before
	 * adding any row values
	 * 
	 * @param syntax use {@link SMIConstants}
	 * @param access
	 * @return
	 */
	public MOTableBuilder addColumnType(int syntax, MOAccess access) {
		colTypeCnt++;

		MOColumn Mc  =new MOColumn(colTypeCnt, syntax, access);
		Mc.setAccess(MOAccessImpl.ACCESS_READ_WRITE);
		columns.add(Mc);
		return this;
	}

	
	public MOTableBuilder addRowValue(Variable variable) {
		if (tableRows.size() == currentRow) {
			tableRows.add(new Variable[columns.size()]);
		}
		tableRows.get(currentRow)[currentCol] = variable;
		currentCol++;
		if (currentCol >= columns.size()) {
			currentRow++;
			currentCol = 0;
		}
		return this;
	}



	public MOTable build(int[] indexes) {
		MOTable ifTable = new DefaultMOTable(tableRootOid, indexDef,
				columns.toArray(new MOColumn[0]));


		MOMutableTableModel model = (MOMutableTableModel) ifTable.getModel();

		int i = 0;
		UniversalVariables UV = UniversalVariables.getInstance();

		String[] oidporpontos = String.valueOf(tableRootOid).split(Pattern.quote("."));
		String objecto = oidporpontos[6];
		if (objecto.equals("3")){
			UV.Put_Table_3(model);
		}
		for (Variable[] variables : tableRows) {
			model.addRow(new DefaultMOMutableRow2PC(new OID(String.valueOf(indexes[i])),
					variables));

			i++;
		}

		ifTable.addMOChangeListener(this);
		ifTable.addMOTableRowListener(this);
		return ifTable;
	}


	@Override
	public void beforePrepareMOChange(MOChangeEvent moChangeEvent) {

	}

	@Override
	public void afterPrepareMOChange(MOChangeEvent moChangeEvent) {

	}

	@Override
	public void beforeMOChange(MOChangeEvent moChangeEvent) {

	}

	@Override
	public void afterMOChange(MOChangeEvent moChangeEvent) {
		moChangeEvent.getChangedObject();
		Variable smi = moChangeEvent.getNewValue();
		OID oc = 	moChangeEvent.getOID();
		Variable mc = moChangeEvent.getOldValue();
		System.out.println(smi);
		System.out.println(oc);
		String OID = oc.toString();
		String[] oidporpontos = OID.split(Pattern.quote("."));
		System.out.println(Arrays.toString(oidporpontos));

	}

	@Override
	public void rowChanged(MOTableRowEvent moTableRowEvent) {

		MOColumn col = moTableRowEvent.getTable().getColumn(1);
		System.out.printf(col.toString());

	}
}
