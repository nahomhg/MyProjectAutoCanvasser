package learningprogramming.academy.autocanvasser.voters;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.type.LatLng;

import java.util.List;

import learningprogramming.academy.autocanvasser.R;

public class VotersAdapter extends ArrayAdapter<Voters> {

    private LatLng latLng;

    public VotersAdapter(Context context, List<Voters> list){
        super(context, 0, list);
    }


    @Override
    public View getView(int pos, View view, ViewGroup parent){
        if(view == null){
            view = ((Activity) getContext()).getLayoutInflater().inflate(R.layout.list_item_details, parent, false);
        }

        TextView voterName = view.findViewById(R.id.voterName);
        TextView voterAddress = view.findViewById(R.id.voterAddress);

        Voters voters = (Voters) getItem(pos);

        voterName.setText(voters.getFirstName()+" "+voters.getLastName());
        //voterAddress.setText("Lat="+userLocation.latitude+" Lng="+userLocation.longitude);
        voterAddress.setText(voters.getFullAddress());
        return view;
    }


    public void notifyDataSetChasnged() {
    }
}