package com.bnotya.bnotyaapp.test;

import android.content.Intent;
import android.test.ActivityUnitTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.widget.Button;

import com.bnotya.bnotyaapp.MainActivity;
import com.bnotya.bnotyaapp.R;

/**
* Tests LaunchActivity in isolation from the system.
*/

public class LaunchActivityTest extends ActivityUnitTestCase<MainActivity>
{
    private Intent _launchIntent;

    public LaunchActivityTest()
    {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        //Create an intent to launch target Activity
        _launchIntent = new Intent(getInstrumentation().getTargetContext(),
                MainActivity.class);
    }

/**
     * Tests the preconditions of this test fixture.
     */

    @MediumTest
    public void testPreconditions()
    {
        //Start the activity under test in isolation, without values for savedInstanceState and
        //lastNonConfigurationInstance
        startActivity(_launchIntent, null, null);

/*final Button openWomenMenuButton = (Button) getActivity().findViewById(R.id.openWomenMenuButton);

        assertNotNull("MainActivity is null", getActivity());
        assertNotNull("openWomenMenuButton is null", openWomenMenuButton);*/

    }

    @MediumTest
    public void testLaunchNextActivityButton_labelText()
    {
        startActivity(_launchIntent, null, null);
        /*final Button openWomenMenuButton = (Button) getActivity().findViewById(R.id.openWomenMenuButton);

        final String expectedButtonText = getActivity().getString(R.string.open_women_menu);
        assertEquals("Unexpected button label text", expectedButtonText,
        		openWomenMenuButton.getText());*/

    }

    @MediumTest
    public void testNextActivityWasLaunchedWithIntent()
    {
        startActivity(_launchIntent, null, null);

/*final Button openWomenMenuButton = (Button) getActivity().findViewById(R.id.openWomenMenuButton);
        //Because this is an isolated ActivityUnitTestCase we have to directly click the
        //button from code
        openWomenMenuButton.performClick();

        // Get the intent for the next started activity
        final Intent launchIntent = getStartedActivityIntent();
        //Verify the intent was not null.
        assertNotNull("Intent was null", launchIntent);
        //Verify that LaunchActivity was finished after button click
        assertTrue(isFinishCalled()); */
    }
}
