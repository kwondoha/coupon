local remaining = tonumber(redis.call('GET', KEYS[1]) or '0')
if remaining <= 0 then
    return
end
redis.call('DECR', KEYS[1])
return 1