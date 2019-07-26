package gov.nih.tbi.repository.model;

import java.io.Serializable;
import java.util.ArrayList;

import javax.xml.bind.annotation.adapters.XmlAdapter;

//may not need this
public class InstancedRecordMapAdapter extends XmlAdapter<InstancedRow[], ArrayList<InstancedRow>> implements Serializable
{
    private static final long serialVersionUID = -1274046647161875695L;

    @Override
    public ArrayList<InstancedRow> unmarshal(InstancedRow[] array) throws Exception
    {
        ArrayList<InstancedRow> toReturn = new ArrayList<InstancedRow>();
        for ( InstancedRow e : array )
        {
            toReturn.add(e);
        }
        return toReturn;
    }

    @Override
    public InstancedRow[] marshal(ArrayList<InstancedRow> map) throws Exception
    {
        InstancedRow[] toReturn = new InstancedRow[map.size()];
        int index = 0;
        for ( InstancedRow iR : map )
        {
//            element.instancedRow = entry.getValue();
            toReturn[index++] = iR;
        }
        return toReturn;
    }

}
