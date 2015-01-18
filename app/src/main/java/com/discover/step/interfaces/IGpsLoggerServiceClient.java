package com.discover.step.interfaces;

import android.location.Location;

/**
 * Created by Geri on 2015.01.18..
 */
public interface IGpsLoggerServiceClient {

    /**
     * A new location fix has been obtained.
     *
     * @param loc
     */
    public void OnLocationUpdate(Location loc);

    /**
     * Asking the calling activity form to clear itself.
     */
    public void OnStartLogging();

    /**
     * Asking the calling activity form to indicate that logging has stopped
     */
    public void OnStopLogging();
}
