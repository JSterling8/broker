/**
 * UnknownMessageFormatException is an exception thrown by subtypes of
 * MessageHandler when a message can't be parsed by that particular
 * subtype.
 * <br>
 * This source code is copyright 2005 by Patrick May.  All
 * rights reserved.
 *
 * @author Patrick May (patrick@softwarematters.org)
 * @author &copy; 2005 Patrick May.  All rights reserved.
 * @version 1
 */

public class UnknownMessageFormatException extends Exception
{
    /**
     * The full constructor for the UnknownMessageFormatException class.
     */
    public UnknownMessageFormatException(String message)
    {
        super(message);
    }  // end UnknownMessageFormatException::UnknownMessageFormatException()
}  // end UnknownMessageFormatException