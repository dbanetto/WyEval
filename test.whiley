function test() -> (int r)
    ensures r == 0:
    int i = 10
    //
    while i > 0
        where i >= 0 && i <= 10:
        i = i - 1
    //
    return i
