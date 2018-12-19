# Extra files for account linking

This folder contains files needed so that external authentication providers can be used to login to your Casa installation:

- `login.xhtml`: Place this file in `/opt/gluu/jetty/oxauth/custom/pages`. This will "override" the default login page of your Gluu Server.
- `Casa.py`: Replace the contents of Casa custom script with this file. Backup original contents first somewhere.

Ensure you have already installed and configured Passport so that the integration with external providers is already working. Check passport docs to learn how to do so.
