package Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.abdul.pucitstudentportalsystem.ChatFragment;
import com.example.abdul.pucitstudentportalsystem.PeopleFragment;
import com.example.abdul.pucitstudentportalsystem.RequestFragment;

public class SectionPagerAdapter extends FragmentPagerAdapter {

    public SectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 2:
                RequestFragment requestFragment= new RequestFragment();
                return requestFragment;
            case 0:
                ChatFragment chatFragment= new ChatFragment();
                return chatFragment;
            case 1:
                PeopleFragment peopleFragment= new PeopleFragment();
                return peopleFragment;
            default:
                return null;

        }
    }

    @Override
    public int getCount() {
        return 3;
    }
    public CharSequence getPageTitle(int position)
    {
        switch (position){
            case 2:
                return "REQUESTS";
            case 0:
                return "CHAT";
            case 1:
                return "PEOPLE";
            default:
                    return null;

        }
    }

}
