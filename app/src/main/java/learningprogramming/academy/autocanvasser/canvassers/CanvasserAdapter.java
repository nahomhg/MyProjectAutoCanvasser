package learningprogramming.academy.autocanvasser.canvassers;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import learningprogramming.academy.autocanvasser.R;

public class CanvasserAdapter extends ArrayAdapter<Canvassers> {

    public CanvasserAdapter(Context context, List<Canvassers> list){
        super(context,0, list);
    }

    @Override
    public View getView(int pos, View view, ViewGroup parent){
        if(view == null){
            view = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.canvassers_awaiting_approval_item_list, parent, false);
        }

        TextView canvasserName = view.findViewById(R.id.canvasserName);
        TextView canvasserEmailaddress = view.findViewById(R.id.canvasserEmailAddress);

        Canvassers canvassers = (Canvassers) getItem(pos);

        canvasserName.setText(canvassers.getFirstName()+" "+canvassers.getLastName());
        //voterAddress.setText("Lat="+userLocation.latitude+" Lng="+userLocation.longitude);
        canvasserEmailaddress.setText(canvassers.getEmail());
        return view;
    }
}
