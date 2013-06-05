<?php
$fin = @fopen($argv[1], "r");
$fout = @fopen($argv[2], "w");
if ($fin && $fout) {
    while (($s = fgets($fin)) !== false) {
        if (trim($s)!=="") {
			$a = explode("\t", $s);
			
			fwrite($fout, implode("\t", array($a[1], $a[4][0], $a[2], $a[4])).PHP_EOL);
		} else {
			fwrite($fout, PHP_EOL);
		}
    }
    fclose($fin);
	fclose($fout);
} else {
	echo "Error loading files";
}