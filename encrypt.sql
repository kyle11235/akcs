--Encrypt:
-- SELECT ENCRYPTDECRYPT.ENCRYPT('kyle.z.zhang@xxx.com|100') FROM dual;

--Decrypt:
-- SELECT ENCRYPTDECRYPT.DECRYPT('xxxxxxxxxxxxxxxxxxxxxxxxxxxxxx') FROM dual;


CREATE OR REPLACE PACKAGE ENCRYPTDECRYPT AS

  FUNCTION encrypt (p_text  IN  VARCHAR2) RETURN RAW;

  FUNCTION decrypt (p_raw  IN  RAW) RETURN VARCHAR2;

END ENCRYPTDECRYPT;


CREATE OR REPLACE PACKAGE BODY ENCRYPTDECRYPT AS

  lr_key     RAW(255)  := UTL_RAW.cast_to_raw('starpass');

  FUNCTION encrypt (p_text  IN  VARCHAR2) RETURN RAW
  IS
    lc_text       VARCHAR2(32767) := p_text;
    lt_enc_text   RAW(32767);
  BEGIN

    lc_text := RPAD( lc_text, (TRUNC(LENGTH(lc_text)/8)+1)*8, CHR(0) ); 

    DBMS_OBFUSCATION_TOOLKIT.desencrypt(input => UTL_RAW.cast_to_raw(lc_text),
                                        key   => lr_key,
                              encrypted_data  => lt_enc_text);
    RETURN lt_enc_text;
  END;

  FUNCTION decrypt (p_raw  IN  RAW) RETURN VARCHAR2 IS

    lc_decrypted     VARCHAR2(32767);
    lc_return_dec    VARCHAR2(32767);
  BEGIN
    DBMS_OBFUSCATION_TOOLKIT.desdecrypt(input => p_raw,
                                       key    => lr_key,
                              decrypted_data  => lc_decrypted);

    lc_return_dec := UTL_RAW.cast_to_varchar2(lc_decrypted);

    RETURN RTRIM( lc_return_dec, CHR(0) );

  END;

END ENCRYPTDECRYPT;